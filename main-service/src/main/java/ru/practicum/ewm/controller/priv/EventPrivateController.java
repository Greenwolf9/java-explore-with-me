package ru.practicum.ewm.controller.priv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.model.StateAction;
import ru.practicum.ewm.service.event.EventService;
import ru.practicum.ewm.service.request.RequestService;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@RequestMapping("/users/{userId}/events")
@RestController
@Slf4j
public class EventPrivateController {
    private final EventService eventService;
    private final RequestService requestService;


    @Autowired
    public EventPrivateController(EventService eventService, RequestService requestService) {
        this.eventService = eventService;
        this.requestService = requestService;
    }

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping
    public ResponseEntity<EventFullDto> saveEvent(@PathVariable("userId") Long userId,
                                                  @RequestBody @Valid NewEventDto newEventDto, BindingResult bindingResult) throws NotFoundException, IncorrectConditionException {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Field: name. Error: must not be blank. Value: null");
        }
        if (LocalDateTime.parse(newEventDto.getEventDate(), formatter).isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IncorrectConditionException("Field: eventDate. Error: должно содержать дату, " +
                    "которая еще не наступила. Value: " + newEventDto.getEventDate());
        }
        log.info("Post event userId {}, event {} ", userId, newEventDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventService.saveEvent(userId, newEventDto));
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable("userId") Long userId,
                                 @PathVariable("eventId") Long eventId) throws NotFoundException {
        log.info("Get event by id {} posted by user {} ", eventId, userId);
        return eventService.getEventOfUserById(userId, eventId);
    }

    @GetMapping
    public List<EventFullDto> getAllEvents(@PathVariable("userId") Long userId) {
        log.info("Get all events posted by user {} ", userId);
        return eventService.getAllEvents(userId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable("userId") Long userId,
                                    @PathVariable("eventId") Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) throws NotFoundException,
            IncorrectConditionException {

        if (!Objects.equals(updateEventUserRequest.getStateAction(), StateAction.CANCEL_REVIEW.name())
                && (!Objects.equals(updateEventUserRequest.getStateAction(), StateAction.SEND_TO_REVIEW.name()))) {
            throw new IncorrectConditionException("Only pending or canceled events can be changed");
        }
        log.info("Patch event {} by id {} posted by user {} ", updateEventUserRequest, eventId, userId);
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestsForEventOfUser(@PathVariable("userId") Long userId,
                                                                      @PathVariable("eventId") Long eventId) {
        log.info("Get list of requests of user {} for event {}", userId, eventId);
        return requestService.getAllRequestsForEventOfUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusOfParticipationRequest(@PathVariable("userId") Long userId,
                                                                             @PathVariable("eventId") Long eventId,
                                                                             @RequestBody EventRequestStatusUpdateRequest request)
            throws NotFoundException, IncorrectConditionException {
        log.info("Patch requests {} of event {} ", request, eventId);
        return eventService.updateStatusOfParticipationRequest(userId, eventId, request);
    }
}
