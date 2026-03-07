package com.neurixa.domain.blog.shared;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
