package com.neurixa.core.event;

import java.util.List;

/**
 * Mixin interface for aggregates that record domain events.
 * 
 * An aggregate that implements this interface can record domain events that
 * occurred as a result of domain operations. These events can then be published
 * to event handlers for side effects, auditing, and event sourcing.
 * 
 * Usage:
 * <pre>
 * public class Article implements AggregateWithEvents {
 *     private List<DomainEvent> domainEvents = new ArrayList<>();
 *     
 *     public void publish() {
 *         this.status = ArticleStatus.PUBLISHED;
 *         recordEvent(new ArticlePublishedEvent(this.id, this.title));
 *     }
 *     
 *     @Override
 *     public void recordEvent(DomainEvent event) {
 *         domainEvents.add(event);
 *     }
 * }
 * </pre>
 */
public interface AggregateWithEvents {
    /**
     * Record a domain event that occurred during a domain operation.
     * 
     * @param event The domain event to record
     */
    void recordEvent(DomainEvent event);

    /**
     * Get all recorded domain events.
     * 
     * @return A list of domain events
     */
    List<DomainEvent> getDomainEvents();

    /**
     * Clear all recorded domain events.
     * 
     * This is typically called after events have been published/persisted.
     */
    void clearDomainEvents();
}
