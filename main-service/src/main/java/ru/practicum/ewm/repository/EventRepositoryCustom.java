package ru.practicum.ewm.repository;

import ru.practicum.ewm.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {

    List<Event> findFilteredEvents(String text,
                                   List<Long> categories,
                                   boolean paid, LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd, String sort,
                                   int from, int size);

}
