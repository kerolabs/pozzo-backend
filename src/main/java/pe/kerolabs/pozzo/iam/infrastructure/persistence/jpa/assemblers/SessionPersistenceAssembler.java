package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.assemblers;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Session;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.SessionPersistenceEntity;

/**
 * Static assembler between the session aggregate and its persistence entity.
 */
public final class SessionPersistenceAssembler {

    private SessionPersistenceAssembler() {
    }

    public static Session toDomainFromPersistence(SessionPersistenceEntity entity) {
        var session = new Session();
        session.restoreState(
                entity.getId(),
                entity.getAccountId(),
                entity.getTokenHash(),
                entity.getDeviceLabel(),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt());
        return session;
    }

    /**
     * Copies the state of the aggregate onto a new or already loaded persistence entity.
     */
    public static SessionPersistenceEntity toPersistenceFromDomain(Session session, SessionPersistenceEntity entity) {
        entity.setId(session.getId());
        entity.setAccountId(session.getAccountId());
        entity.setTokenHash(session.getTokenHash());
        entity.setDeviceLabel(session.getDeviceLabel());
        entity.setIssuedAt(session.getIssuedAt());
        entity.setExpiresAt(session.getExpiresAt());
        entity.setRevokedAt(session.getRevokedAt());
        return entity;
    }
}
