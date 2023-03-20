package ru.practicum.ewm.service.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mappers.RequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestServiceImpl implements RequestService {

    @Autowired
    protected RequestMapper requestMapper;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public RequestServiceImpl(RequestRepository requestRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public ParticipationRequestDto saveRequest(Long userId, Long eventId) throws NotFoundException, DataIntegrityViolationException, IncorrectConditionException {
        final Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " doesn't exist."));
        final User requester = userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " doesn't exist."));

        if (requestRepository.existsParticipationRequestByRequesterAndEvent(userId, event.getId())) {
            throw new DataIntegrityViolationException("Integrity constraint has been violated.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new IncorrectConditionException("Conditions are not met.");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new DataIntegrityViolationException("Event should be published");
        }
        int confirmedRequests = requestRepository.countByStatus(eventId, Status.CONFIRMED);
        ParticipationRequest participationRequest = new ParticipationRequest();
        if (event.getParticipantLimit() > confirmedRequests) {
            participationRequest.setRequester(requester);
            participationRequest.setEvent(event);
            participationRequest.setCreated(LocalDateTime.now().withNano(0));
            if (!event.isRequestModeration()) {
                participationRequest.setStatus(Status.CONFIRMED);
            } else {
                participationRequest.setStatus(Status.PENDING);
            }
        } else {
            throw new IncorrectConditionException("Limit of participants is exceeded.");
        }
        return requestMapper.mapRequestToDto(requestRepository.save(participationRequest));
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestsByUserId(Long userId) throws NotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " doesn't exist.");
        }
        return requestRepository.findAllByRequesterId(userId)
                .stream().map(requestMapper::mapRequestToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto updateRequest(Long userId, Long requestId) throws NotFoundException {
        final ParticipationRequest participationRequest = requestRepository
                .findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " wasn't found."));
        if (participationRequest.getRequester().getId().equals(userId)) {
            participationRequest.setStatus(Status.CANCELED);
        }
        return requestMapper.mapRequestToDto(requestRepository.save(participationRequest));
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestsForEventOfUser(Long userId, Long eventId) {
        return requestRepository
                .findAllByEventId(userId, eventId)
                .stream()
                .map(requestMapper::mapRequestToDto)
                .collect(Collectors.toList());
    }
}
