package pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base JPA persistence entity for every persistence entity that requires identity and auditing.
 *
 * <p>Provides a {@code UUID} identifier, as the data model of every bounded context uses,
 * plus {@code createdAt} and {@code updatedAt}. It lives in the infrastructure layer to keep
 * JPA and auditing concerns out of the domain model.</p>
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAbstractPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Sets the id. Used by assemblers when rebuilding a persistence entity
     * from a domain object that already carries an identity.
     *
     * @param id the persistence identity to assign
     */
    public void setId(UUID id) {
        this.id = id;
    }
}
