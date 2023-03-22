package ru.practicum.ewm.controller.priv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.rating.NewRatingDto;
import ru.practicum.ewm.dto.rating.RatingDto;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.service.rating.RatingService;

import javax.validation.ValidationException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users/{userId}/events/{eventId}/likes")
public class RatingPrivateController {
    private final RatingService ratingService;

    @Autowired
    public RatingPrivateController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<RatingDto> saveRating(@PathVariable("userId") Long userId,
                                                @PathVariable("eventId") Long eventId,
                                                @RequestBody NewRatingDto newRatingDto) throws NotFoundException, IncorrectConditionException {
        log.info("Post rating {} ", newRatingDto);
        if (newRatingDto.getStars() == 0) {
            throw new ValidationException("Field: stars. Error: must not be blank. Value: 0");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ratingService.saveRating(userId, eventId, newRatingDto));
    }

    @GetMapping("/{id}")
    public RatingDto getRatingById(@PathVariable("userId") Long userId,
                                   @PathVariable("eventId") Long eventId,
                                   @PathVariable("id") Long id) throws NotFoundException {
        log.info("Get rating id {} of event {}, by user {} ", id, eventId, userId);
        return ratingService.getRatingById(userId, eventId, id);
    }

    @GetMapping
    public List<RatingDto> getAllRatingByEventId(@PathVariable("userId") Long userId,
                                                 @PathVariable("eventId") Long eventId) {
        return ratingService.getAllRatingsByEventId(userId, eventId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RatingDto> deleteRating(@PathVariable("userId") Long userId,
                                                  @PathVariable("eventId") Long eventId,
                                                  @PathVariable("id") Long id) {
        log.info("Delete rating by id {} ", id);
        ratingService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }
}
