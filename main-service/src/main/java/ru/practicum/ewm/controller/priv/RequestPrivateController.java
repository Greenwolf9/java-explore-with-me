package ru.practicum.ewm.controller.priv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.service.request.RequestService;

import java.util.List;

@RequestMapping("/users/{userId}/requests")
@RestController
@Slf4j
public class RequestPrivateController {

    private final RequestService requestService;

    @Autowired
    public RequestPrivateController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> saveRequest(@PathVariable("userId") Long userId,
                                                               @RequestParam(value = "eventId") Long eventId)
            throws NotFoundException, DataIntegrityViolationException, IncorrectConditionException {
        log.info("Post request by user {} for event {} ", userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestService.saveRequest(userId, eventId));
    }

    @GetMapping
    List<ParticipationRequestDto> getAllRequestByUserId(@PathVariable("userId") Long userId) throws NotFoundException {
        log.info("Get list of requests by user {} ", userId);
        return requestService.getAllRequestsByUserId(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto updateRequest(@PathVariable("userId") Long userId,
                                                 @PathVariable("requestId") Long requestId) throws NotFoundException {
        log.info("Patch request {} of user {} to be canceled ", userId, requestId);
        return requestService.updateRequest(userId, requestId);
    }
}
