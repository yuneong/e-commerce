package com.loopers.infrastructure.eventHandled;

import com.loopers.domain.eventHandled.EventHandled;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventHandledJpaRepository extends JpaRepository<EventHandled, Long> {

    boolean existsByEventId(String eventId);

}
