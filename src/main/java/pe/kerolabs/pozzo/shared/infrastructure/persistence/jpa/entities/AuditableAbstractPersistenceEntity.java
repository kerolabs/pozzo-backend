package pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base JPA persistence entity for the tables that record when a row was created and last updated.
 *
 * <p>Adds {@code createdAt} and {@code updatedAt} to the identity of {@link AbstractPersistenceEntity}.
 * Both are filled in by Spring Data auditing. It lives in the infrastructure layer to keep JPA and
 * auditing concerns out of the domain model.</p>
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAbstractPersistenceEntity extends AbstractPersistenceEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
