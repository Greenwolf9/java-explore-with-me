package ru.practicum.ewm.service.event;

import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;

public interface EventService {
    EventFullDto saveEvent(Long userId, NewEventDto newEventDto) throws NotFoundException;

    EventFullDto getEventOfUserById(Long userId, Long eventId) throws NotFoundException;

    List<EventFullDto> getAllEvents(Long userId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) throws NotFoundException;

    EventRequestStatusUpdateResult updateStatusOfParticipationRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request) throws NotFoundException, IncorrectConditionException;

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) throws NotFoundException, IncorrectConditionException;


    EventFullDto getEventById(Long id) throws NotFoundException, DataIntegrityViolationException;

    List<EventFullDto> findListOfEventByParameters(List<Long> users,
                                                   List<String> states,
                                                   List<Long> categories,
                                                   String rangeStart,
                                                   String rangeEnd,
                                                   int from,
                                                   int size);

    List<EventShortDto> getPublicListOfEventWithParams(String text,
                                                       List<Long> categories,
                                                       boolean paid,
                                                       String rangeStart,
                                                       String rangeEnd,
                                                       boolean onlyAvailable,
                                                       String sort, int from, int size);
}
