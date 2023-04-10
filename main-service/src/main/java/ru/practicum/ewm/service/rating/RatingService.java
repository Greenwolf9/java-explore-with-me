package ru.practicum.ewm.service.rating;

import ru.practicum.ewm.dto.rating.NewRatingDto;
import ru.practicum.ewm.dto.rating.RatingDto;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;

public interface RatingService {

    RatingDto saveRating(Long userId, Long eventId, NewRatingDto newRatingDto) throws NotFoundException, IncorrectConditionException;

    RatingDto getRatingById(Long userId, Long eventId, Long id) throws NotFoundException;

    List<RatingDto> getAllRatingsByEventId(Long userId, Long eventId);

    void deleteRating(Long id);
}
