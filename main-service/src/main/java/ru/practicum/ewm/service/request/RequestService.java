package ru.practicum.ewm.service.request;

import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto saveRequest(Long userId, Long eventId) throws NotFoundException, DataIntegrityViolationException, IncorrectConditionException;

    List<ParticipationRequestDto> getAllRequestsByUserId(Long userId) throws NotFoundException;

    ParticipationRequestDto updateRequest(Long userId, Long requestId) throws NotFoundException;

    List<ParticipationRequestDto> getAllRequestsForEventOfUser(Long userId, Long eventId);
}
