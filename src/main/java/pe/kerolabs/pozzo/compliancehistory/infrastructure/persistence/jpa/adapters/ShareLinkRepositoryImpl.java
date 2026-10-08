package pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.ShareLink;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ShareToken;
import pe.kerolabs.pozzo.compliancehistory.domain.repositories.ShareLinkRepository;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.entities.ShareLinkPersistenceEntity;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.repositories.ShareLinkPersistenceRepository;

import java.util.Optional;

/**
 * Adapter that implements the share link repository port with Spring Data JPA.
 */
@Repository
public class ShareLinkRepositoryImpl implements ShareLinkRepository {

    private final ShareLinkPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ShareLinkRepositoryImpl(ShareLinkPersistenceRepository persistenceRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<ShareLink> findByToken(ShareToken token) {
        return persistenceRepository.findByToken(token.value()).map(ShareLinkRepositoryImpl::toDomain);
    }

    @Override
    public ShareLink save(ShareLink link) {
        var entity = persistenceRepository.findById(link.getId()).orElseGet(ShareLinkPersistenceEntity::new);
        entity.setId(link.getId());
        entity.setToken(link.getToken().value());
        entity.setAccountId(link.getAccountId());
        entity.setCreatedAt(link.getCreatedAt());
        entity.setExpiresAt(link.getExpiresAt());
        entity.setRevoked(link.isRevoked());
        var saved = persistenceRepository.save(entity);
        link.domainEvents().forEach(eventPublisher::publishEvent);
        link.clearDomainEvents();
        return toDomain(saved);
    }

    private static ShareLink toDomain(ShareLinkPersistenceEntity entity) {
        var link = new ShareLink();
        link.restoreState(entity.getId(), new ShareToken(entity.getToken()), entity.getAccountId(),
                entity.getCreatedAt(), entity.getExpiresAt(), entity.isRevoked());
        return link;
    }
}
