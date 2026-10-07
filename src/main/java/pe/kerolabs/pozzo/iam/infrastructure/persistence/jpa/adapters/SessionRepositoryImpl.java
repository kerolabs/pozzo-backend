package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.Session;
import pe.kerolabs.pozzo.iam.domain.repositories.SessionRepository;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.assemblers.SessionPersistenceAssembler;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.SessionPersistenceEntity;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.repositories.SessionPersistenceRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the session repository port with Spring Data JPA
 * and publishes the domain events of each saved aggregate.
 */
@Repository
public class SessionRepositoryImpl implements SessionRepository {

    private final SessionPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SessionRepositoryImpl(SessionPersistenceRepository persistenceRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Session> findById(UUID id) {
        return persistenceRepository.findById(id).map(SessionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Session> findByTokenHash(String tokenHash) {
        return persistenceRepository.findByTokenHash(tokenHash)
                .map(SessionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Session save(Session session) {
        var entity = persistenceRepository.findById(session.getId()).orElseGet(SessionPersistenceEntity::new);
        var saved = persistenceRepository.save(SessionPersistenceAssembler.toPersistenceFromDomain(session, entity));
        session.domainEvents().forEach(eventPublisher::publishEvent);
        session.clearDomainEvents();
        return SessionPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
