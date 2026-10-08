package pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.MemberRecord;
import pe.kerolabs.pozzo.compliancehistory.domain.model.entities.ComplianceEntry;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;
import pe.kerolabs.pozzo.compliancehistory.domain.repositories.MemberRecordRepository;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.entities.ComplianceEntryPersistenceEntity;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.entities.MemberRecordPersistenceEntity;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.repositories.ComplianceEntryPersistenceRepository;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.repositories.MemberRecordPersistenceRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adapter that implements the member record repository port with Spring Data JPA and publishes the
 * domain events of each saved aggregate. Entries are only ever added, never changed.
 */
@Repository
public class MemberRecordRepositoryImpl implements MemberRecordRepository {

    private final MemberRecordPersistenceRepository recordRepository;
    private final ComplianceEntryPersistenceRepository entryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MemberRecordRepositoryImpl(MemberRecordPersistenceRepository recordRepository,
                                      ComplianceEntryPersistenceRepository entryRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.recordRepository = recordRepository;
        this.entryRepository = entryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<MemberRecord> findByAccountId(UUID accountId) {
        return recordRepository.findById(accountId).map(MemberRecordRepositoryImpl::toDomain);
    }

    @Override
    public boolean existsEntryBySourceEventId(String sourceEventId) {
        return entryRepository.existsBySourceEventId(sourceEventId);
    }

    @Override
    public MemberRecord save(MemberRecord record) {
        var entity = recordRepository.findById(record.getAccountId()).orElseGet(MemberRecordPersistenceEntity::new);
        var summary = record.getSummary();
        entity.setId(record.getAccountId());
        entity.setOnTimeCount(summary.onTime());
        entity.setLateCount(summary.late());
        entity.setCoveredCount(summary.covered());
        entity.setRejectedCount(summary.rejected());
        entity.setDropoutCount(summary.dropouts());
        entity.setCyclesCompleted(summary.cyclesCompleted());
        entity.setLevel(summary.level());
        entity.setUpdatedAt(record.getUpdatedAt());
        var existing = entity.getEntries().stream()
                .collect(Collectors.toMap(ComplianceEntryPersistenceEntity::getId, Function.identity()));
        for (var entry : record.getEntries()) {
            if (!existing.containsKey(entry.getId())) {
                var entryEntity = new ComplianceEntryPersistenceEntity();
                entryEntity.setId(entry.getId());
                entryEntity.setRecord(entity);
                entryEntity.setCycleId(entry.getCycleId());
                entryEntity.setGroupId(entry.getGroupId());
                entryEntity.setGroupName(entry.getGroupName());
                entryEntity.setPeriodId(entry.getPeriodId());
                entryEntity.setKind(entry.getKind());
                entryEntity.setOccurredAt(entry.getOccurredAt());
                entryEntity.setSourceEventId(entry.getSourceEventId());
                entity.getEntries().add(entryEntity);
            }
        }
        var saved = recordRepository.save(entity);
        record.domainEvents().forEach(eventPublisher::publishEvent);
        record.clearDomainEvents();
        return toDomain(saved);
    }

    private static MemberRecord toDomain(MemberRecordPersistenceEntity entity) {
        var entries = entity.getEntries().stream()
                .map(item -> {
                    var entry = new ComplianceEntry();
                    entry.restoreState(item.getId(), item.getCycleId(), item.getGroupId(), item.getGroupName(),
                            item.getPeriodId(), item.getKind(), item.getOccurredAt(), item.getSourceEventId());
                    return entry;
                })
                .toList();
        var summary = new ComplianceSummary(entity.getOnTimeCount(), entity.getLateCount(), entity.getCoveredCount(),
                entity.getRejectedCount(), entity.getDropoutCount(), entity.getCyclesCompleted(), entity.getLevel());
        var record = new MemberRecord();
        record.restoreState(entity.getId(), entries, summary, entity.getUpdatedAt());
        return record;
    }
}
