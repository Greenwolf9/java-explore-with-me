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
import ru.practicum.ewm.dto.rating.Score;
import ru.practicum.ewm.dto.request.ConfirmedRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mappers.EventMapper;
import ru.practicum.ewm.mappers.RequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.*;
import ru.practicum.ewm.service.rating.RatingCalculator;

import javax.validation.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final RatingRepository ratingRepository;

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
                            RequestRepository requestRepository,
                            RatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;
        this.ratingRepository = ratingRepository;

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
        EventFullDto dto = eventMapper.eventToFullDto(event);
        dto.setConfirmedRequests((long) requestRepository.countByStatus(event.getId(), Status.CONFIRMED));
        if (ratingRepository.existsByEventIdAndReviewerId(eventId, userId)) {
            dto.setRating(RatingCalculator.calcScore(ratingRepository.totalScore(eventId)));
        }
        return dto;
    }

    @Override
    public List<EventFullDto> getAllEvents(Long userId) {
        List<Long> ids = eventRepository.findIdsOfEventsByInitiator(userId);
        Map<Long, Long> confirmedEvents = requestRepository
                .findAllByListOfEventIds(ids, Status.CONFIRMED).stream().collect(Collectors.toMap(ConfirmedRequest::getEventId, ConfirmedRequest::getConfirmed));
        Map<Long, EventFullDto> allEventsOfUser = eventRepository
                .findAllByInitiatorId(userId)
                .stream().map(eventMapper::eventToFullDto)
                .collect(Collectors.toMap(EventFullDto::getId, Function.identity()));
        for (Long id : allEventsOfUser.keySet()) {
            if (confirmedEvents.containsKey(id)) {
                allEventsOfUser.get(id).setConfirmedRequests(confirmedEvents.get(id));
            }
        }
        return new ArrayList<>(allEventsOfUser.values());
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
        eventFullDto.setViews((long) findStats("/events/" + id));
        if (ratingRepository.existsByEventIdAndReviewerId(eventFullDto.getId(), eventFullDto.getInitiator().getId())) {
            eventFullDto.setRating(RatingCalculator.calcScore(ratingRepository.totalScore(eventFullDto.getId())));
        }
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

    public List<EventShortDto> findFilteredEvents(String text,
                                                  List<Long> categories,
                                                  boolean paid,
                                                  String rangeStart,
                                                  String rangeEnd,
                                                  boolean onlyAvailable,
                                                  String sort, int from, int size) {

        List<Long> ids = eventRepository.findIdsOfEvents();
        Map<Long, Long> confirmedEvents = requestRepository.findAllByListOfEventIds(ids, Status.CONFIRMED)
                .stream().collect(Collectors.toMap(ConfirmedRequest::getEventId, ConfirmedRequest::getConfirmed));
        Map<Long, Score> scoreMap = new HashMap<>();
        for (Long id : ids) {
            if (ratingRepository.existsByEventId(id)) {
                scoreMap.put(id, ratingRepository.totalScore(id));
            }
        }

        Map<Long, EventFullDto> allPublicEvents = eventRepository
                .findFilteredEvents(text,
                        categories,
                        paid,
                        rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null,
                        rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null, sort,
                        from, size)
                .stream().map(eventMapper::eventToFullDto)
                .peek(e -> e.setViews((long) findStats("/events")))
                .collect(Collectors.toMap(EventFullDto::getId, Function.identity()));

        for (Long id : allPublicEvents.keySet()) {
            if (confirmedEvents.containsKey(id)) {
                allPublicEvents.get(id).setConfirmedRequests(confirmedEvents.get(id));
            }
            if (scoreMap.containsKey(id)) {
                allPublicEvents.get(id).setRating(RatingCalculator.calcScore(scoreMap.get(id)));
            }
        }

        return allPublicEvents.values().stream()
                .filter(a -> !onlyAvailable || a.getConfirmedRequests() < a.getParticipantLimit())
                .map(eventMapper::fromFullToShort)
                .collect(Collectors.toList());
    }

    public List<EventShortDto> findMostPopularEvents(int from, int size) {
        Map<Long, EventFullDto> popularEvents = eventRepository.findAll(PageRequest.of(from / size, size)).getContent()
                .stream().map(eventMapper::eventToFullDto)
                .peek(e -> e.setViews((long) findStats("/events/popular")))
                .collect(Collectors.toMap(EventFullDto::getId, Function.identity()));

        List<Long> ids = eventRepository.findIdsOfEvents();
        Map<Long, Long> confirmedEvents = requestRepository.findAllByListOfEventIds(ids, Status.CONFIRMED)
                .stream().collect(Collectors.toMap(ConfirmedRequest::getEventId, ConfirmedRequest::getConfirmed));
        Map<Long, Score> scoreMap = new HashMap<>();
        for (Long id : ids) {
            if (ratingRepository.existsByEventId(id)) {
                scoreMap.put(id, ratingRepository.totalScore(id));
            }
        }
        for (Long id : popularEvents.keySet()) {
            if (confirmedEvents.containsKey(id)) {
                popularEvents.get(id).setConfirmedRequests(confirmedEvents.get(id));
            }
            if (scoreMap.containsKey(id)) {
                popularEvents.get(id).setRating(RatingCalculator.calcScore(scoreMap.get(id)));
            }
        }
        return popularEvents.values().stream().map(eventMapper::fromFullToShort).filter(e -> e.getRating() > 0.0)
                .sorted(Comparator.comparingDouble(EventShortDto::getRating).reversed())
                .collect(Collectors.toList());
    }
}