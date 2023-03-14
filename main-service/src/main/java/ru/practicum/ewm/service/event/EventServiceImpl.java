package ru.practicum.ewm.service.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mappers.EventMapper;
import ru.practicum.ewm.mappers.RequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    protected EventMapper eventMapper;
    @Autowired
    protected RequestMapper requestMapper;

    public EventServiceImpl(UserRepository userRepository,
                            EventRepository eventRepository,
                            CategoryRepository categoryRepository,
                            RequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;

    }

    @Override
    public EventFullDto saveEvent(Long userId, NewEventDto newEventDto) throws NotFoundException {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " doesn't exist."));
        final Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " doesn't exist."));
        Event event = eventMapper.mapToEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(State.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        return eventMapper.eventToFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventOfUserById(Long userId, Long eventId) throws NotFoundException {
        final Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " doesn't exist."));
        return eventMapper.eventToFullDto(event);
    }

    @Override
    public List<EventFullDto> getAllEvents(Long userId) {
        return eventRepository
                .findAllByInitiatorId(userId)
                .stream().map(eventMapper::eventToFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) throws NotFoundException {
        final Event eventToBeUpdated = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " doesn't exist."));
        if (eventToBeUpdated.getState().equals(State.PUBLISHED)) {
            throw new ValidationException("Event must not be published");
        }
        eventMapper.update(updateEventUserRequest, eventToBeUpdated);
        return eventMapper.eventToFullDto(eventRepository.save(eventToBeUpdated));
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) throws NotFoundException, IncorrectConditionException {
        final Event eventToBeUpdated = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found."));
        if (eventToBeUpdated.getState().equals(State.PUBLISHED) || eventToBeUpdated.getState().equals(State.REJECTED)) {
            throw new IncorrectConditionException("Cannot publish the event because it's not in the right state: PUBLISHED");
        }
        if (updateEventAdminRequest.getEventDate() != null
                && LocalDateTime.parse(updateEventAdminRequest.getEventDate(), formatter).isBefore(LocalDateTime.now())) {
            throw new IncorrectConditionException("Conditions are not met");
        }
        if (updateEventAdminRequest.getParticipantLimit() > 0) {
            eventToBeUpdated.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.isPaid()) {
            eventToBeUpdated.setPaid(true);
        }
        eventMapper.update(updateEventAdminRequest, eventToBeUpdated);
        eventToBeUpdated.setPublishedOn(LocalDateTime.now().withNano(0));
        return eventMapper.eventToFullDto(eventRepository.save(eventToBeUpdated));
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusOfParticipationRequest(Long userId,
                                                                             Long eventId,
                                                                             EventRequestStatusUpdateRequest request)
            throws NotFoundException, IncorrectConditionException {

        final Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " wasn't found."));

        int confirmedRequests = requestRepository.countByStatus(eventId, Status.CONFIRMED);
        List<ParticipationRequest> requestList = requestRepository.findAllByListOfIds(request.getRequestIds());

        for (ParticipationRequest req : requestList) {
            if (req.getStatus().equals(Status.CONFIRMED)) {
                throw new IncorrectConditionException("Request is already confirmed");
            }
            if (event.getParticipantLimit() != 0
                    && event.getParticipantLimit() == confirmedRequests
                    && Status.valueOf(request.getStatus()).equals(Status.CONFIRMED)) {
                throw new IncorrectConditionException("Conditions are not met.");
            }
            req.setStatus(Status.valueOf(request.getStatus()));
            requestRepository.save(req);
        }
        List<ParticipationRequestDto> confirmed = requestRepository.findAllByEventIdAndStatus(event.getId(), Status.CONFIRMED)
                .stream()
                .map(requestMapper::mapRequestToDto)
                .collect(Collectors.toList());
        List<ParticipationRequestDto> rejected = requestRepository.findAllByEventIdAndStatus(event.getId(), Status.REJECTED)
                .stream()
                .map(requestMapper::mapRequestToDto)
                .collect(Collectors.toList());
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    @Override
    public EventFullDto getEventById(Long id) throws NotFoundException, DataIntegrityViolationException {
        final Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found."));
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new DataIntegrityViolationException("Event should be published.");
        }
        EventFullDto eventFullDto = eventMapper.eventToFullDto(event);
        eventFullDto.setConfirmedRequests((long) requestRepository.countByStatus(id, Status.CONFIRMED));
        return eventMapper.eventToFullDto(event);
    }

    @Override
    public List<EventFullDto> findListOfEventByParameters(List<Long> users,
                                                          List<String> states,
                                                          List<Long> categories,
                                                          String rangeStart,
                                                          String rangeEnd,
                                                          int from,
                                                          int size) {
        int page = from / size;
        List<EventFullDto> adminEvents = eventRepository.findListOfEventByParameters(users,
                        states,
                        categories,
                        rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null,
                        rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null,
                        PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(eventMapper::eventToFullDto)
                .collect(Collectors.toList());
        adminEvents.forEach(event -> event.setConfirmedRequests((long) requestRepository.countByStatus(event.getId(), Status.CONFIRMED)));
        return adminEvents;
    }

    @Override
    public List<EventShortDto> getPublicListOfEventWithParams(String text,
                                                              List<Long> categories,
                                                              boolean paid,
                                                              String rangeStart,
                                                              String rangeEnd,
                                                              boolean onlyAvailable,
                                                              String sort, int from, int size) {
        int page = from / size;
        List<Event> listOfParameterisedEvents = new ArrayList<>();
        switch (sort) {
            case "EVENT_DATE":
                listOfParameterisedEvents = eventRepository
                        .findPublicEventsByParamsByEventDate(text,
                                categories,
                                paid,
                                rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null,
                                rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null,
                                PageRequest.of(page, size))
                        .getContent()
                        .stream()
                        .filter(a -> !onlyAvailable || a.getConfirmedRequests() < a.getParticipantLimit())
                        .collect(Collectors.toList());
                listOfParameterisedEvents.forEach(event -> event.setConfirmedRequests((long) requestRepository.countByStatus(event.getId(), Status.CONFIRMED)));
                break;
            case "VIEWS":
                listOfParameterisedEvents = eventRepository
                        .findPublicEventsByParamsByViews(text,
                                categories,
                                paid,
                                rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null,
                                rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null,
                                PageRequest.of(page, size))
                        .getContent().stream()
                        .filter(a -> !onlyAvailable || a.getConfirmedRequests() < a.getParticipantLimit())
                        .collect(Collectors.toList());
                listOfParameterisedEvents.forEach(event -> event.setConfirmedRequests((long) requestRepository.countByStatus(event.getId(), Status.CONFIRMED)));
                break;
        }
        return listOfParameterisedEvents.stream().map(eventMapper::eventToShortDto).collect(Collectors.toList());
    }
}
