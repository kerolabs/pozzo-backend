package pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import lombok.Getter;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

/**
 * Base JPA persistence entity identified by a {@code UUID}.
 *
 * <p>The identifier is assigned by the domain when an aggregate is created, so it is not generated
 * by the database. Implementing {@link Persistable} lets Spring Data tell a new entity from an existing
 * one without relying on a null id: an entity is new until it is persisted or loaded.</p>
 */
@MappedSuperclass
public abstract class AbstractPersistenceEntity implements Persistable<UUID> {

    @Id
    @Getter
    private UUID id;

    @Transient
    private boolean isNew = true;

    /**
     * Sets the id. Used by assemblers when building a persistence entity from a domain object.
     *
     * @param id the identity assigned by the domain
     */
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}
