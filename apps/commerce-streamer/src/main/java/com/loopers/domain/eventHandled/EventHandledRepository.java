package com.loopers.domain.eventHandled;

public interface EventHandledRepository {

    boolean existsByEventId(String eventId);

    EventHandled save(EventHandled eventHandled);

}
