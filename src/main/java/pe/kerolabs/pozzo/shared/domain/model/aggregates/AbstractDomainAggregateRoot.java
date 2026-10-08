package pe.kerolabs.pozzo.shared.domain.model.aggregates;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.Collection;

/**
 * Base class for all domain aggregate roots.
 *
 * <p>Extends Spring Data Commons' {@link AbstractAggregateRoot} to gain domain event
 * registration without pulling in any JPA concern. Identity and auditing are left to the
 * infrastructure layer, where each bounded context maps its aggregates to dedicated
 * persistence entities.</p>
 *
 * @param <T> the concrete aggregate root type
 */
@NullMarked
public abstract class AbstractDomainAggregateRoot<T extends AbstractDomainAggregateRoot<T>>
        extends AbstractAggregateRoot<T> {

    /**
     * Registers a domain event to be published after this aggregate is saved.
     *
     * @param event the domain event to register
     */
    protected void registerDomainEvent(Object event) {
        super.registerEvent(event);
    }

    /**
     * Returns the domain events registered since the last publication.
     * Public so repository adapters can publish them after the aggregate is persisted.
     *
     * @return the registered domain events
     */
    @Override
    public Collection<Object> domainEvents() {
        return super.domainEvents();
    }

    /**
     * Clears the registered domain events.
     * Public so repository adapters can clear them once they are published.
     */
    @Override
    public void clearDomainEvents() {
        super.clearDomainEvents();
    }
}
