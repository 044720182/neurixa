package com.neurixa.core.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events.
 * 
 * Domain events represent something that happened in the domain that is important
 * for other parts of the system to know about. They are immutable and can be used
 * for event sourcing, auditing, and decoupling side effects from business logic.
 * 
 * Benefits:
 * - Decouples domain logic from side effects
 * - Enables event sourcing and audit trails
 * - Allows multiple handlers for same event
 * - Type-safe event inheritance
 * 
 * Example:
 * <pre>
 * public class ArticlePublishedEvent extends DomainEvent {
 *     private final String articleId;
 *     private final String title;
 *     
 *     public ArticlePublishedEvent(String articleId, String title) {
 *         super();
 *         this.articleId = articleId;
 *         this.title = title;
 *     }
 * }
 * </pre>
 */
public abstract class DomainEvent {
    private final String eventId;
    private final Instant occurredAt;

    /**
     * Create a new domain event with auto-generated ID and timestamp.
     */
    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
    }

    /**
     * Get the unique event ID.
     * 
     * @return The event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Get when this event occurred.
     * 
     * @return The instant when the event was created
     */
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * Get the event type name for serialization/logging.
     * 
     * @return The simple class name of the event
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
