package ru.practicum.ewm.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStats;
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

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    protected EventMapper eventMapper;
    @Autowired
    protected RequestMapper requestMapper;
    @Autowired
    protected ObjectMapper mapper;
    @Autowired
    private StatsClient client;

    public EventServiceImpl(UserRepository userRepository,
                            EventRepository eventRepository,
                            CategoryRepository categoryRepository,
                            RequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;

    }

    //private : сохранение события конкретного пользователя
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

    // private : получение конкретного события по идентификатору пользователя
    @Override
    public EventFullDto getEventOfUserById(Long userId, Long eventId) throws NotFoundException {
        final Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " doesn't exist."));
        EventFullDto dto = eventMapper.eventToFullDto(event);
        dto.setConfirmedRequests((long) requestRepository.countByStatus(event.getId(), Status.CONFIRMED));
        return dto;
    }

    // private : получение всех событий по идентификатору пользователя
    @Override
    public List<EventFullDto> getAllEvents(Long userId) {
        List<EventFullDto> allEventsOfUser = eventRepository
                .findAllByInitiatorId(userId)
                .stream().map(eventMapper::eventToFullDto)
                .collect(Collectors.toList());

        allEventsOfUser.forEach(x -> x.setConfirmedRequests((long) requestRepository.countByStatus(x.getId(), Status.CONFIRMED)));
        return allEventsOfUser;
    }

    // private : обновление события по идентификатору события и пользователя
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

    // admin: обновление события администратором
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

    // private: обновление статуса заявки на участии в событии
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

    // public : публичный запрос конкретного события по его айди
    @Override
    public EventFullDto getEventById(Long id) throws NotFoundException, DataIntegrityViolationException {
        final Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found."));
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new DataIntegrityViolationException("Event should be published.");
        }
        EventFullDto eventFullDto = eventMapper.eventToFullDto(event);
        eventFullDto.setConfirmedRequests((long) requestRepository.countByStatus(id, Status.CONFIRMED));
        eventFullDto.setViews((long) findStats("/events/" + id));
        return eventMapper.eventToFullDto(event);
    }

    private int findStats(String uri) {
        ObjectMapper mapper = new ObjectMapper();
        int hits = 0;
        List<String> uris = new ArrayList<>();
        uris.add(uri);
        try {
            Object object = client.stats(LocalDateTime.now().minusDays(32), LocalDateTime.now(), uris, false).getBody();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(object);
            List<ViewStats> viewStats = mapper.readValue(json, new TypeReference<>() {
            });
            for (ViewStats stat : viewStats) {
                hits = hits + stat.getHits();
            }
        } catch (JsonProcessingException e) {
            e.getMessage();
        }
        return hits;
    }

    // admin: получение событий с возможностью фильтрации
    @Override
    public List<EventFullDto> findListOfEventByParameters(List<Long> users,
                                                          List<String> states,
                                                          List<Long> categories,
                                                          String rangeStart,
                                                          String rangeEnd,
                                                          int from,
                                                          int size) {
        int page = from / size;
        return eventRepository.findListOfEventByParameters(users,
                        states,
                        categories,
                        rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null,
                        rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null,
                        PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(eventMapper::eventToFullDto)
                .peek(e -> e.setConfirmedRequests((long) requestRepository.countByStatus(e.getId(), Status.CONFIRMED)))
                .peek(e -> e.setViews((long) findStats("/admin/events")))
                .collect(Collectors.toList());
    }
    // public: получение событий с возможностью фильтрации

    public List<EventShortDto> findFilteredEvents(String text,
                                                  List<Long> categories,
                                                  boolean paid,
                                                  String rangeStart,
                                                  String rangeEnd,
                                                  boolean onlyAvailable,
                                                  String sort, int from, int size) {
        return eventRepository
                .findFilteredEvents(text,
                        categories,
                        paid,
                        rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null,
                        rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null, sort,
                        from, size)
                .stream().map(eventMapper::eventToFullDto)
                .peek(e -> e.setConfirmedRequests((long) requestRepository.countByStatus(e.getId(), Status.CONFIRMED)))
                .peek(e -> e.setViews((long) findStats("/events")))
                .filter(a -> !onlyAvailable || a.getConfirmedRequests() < a.getParticipantLimit())
                .map(dto -> new EventShortDto(dto.getAnnotation(),
                        dto.getCategory(),
                        dto.getConfirmedRequests(),
                        dto.getEventDate(),
                        dto.getId(),
                        dto.getInitiator(),
                        dto.getPaid(),
                        dto.getTitle(),
                        dto.getViews()))
                .collect(Collectors.toList());
    }
}