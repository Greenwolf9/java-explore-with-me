package ru.practicum.ewm.service.rating;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.rating.NewRatingDto;
import ru.practicum.ewm.dto.rating.RatingDto;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mappers.RatingMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Rating;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RatingRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Autowired
    protected RatingMapper ratingMapper;

    @Autowired
    public RatingServiceImpl(RatingRepository ratingRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             RequestRepository requestRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
    }

    public RatingDto saveRating(Long userId, Long eventId, NewRatingDto newRatingDto) throws NotFoundException, IncorrectConditionException {
        final Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " doesn't exist."));
        final User reviewer = userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " doesn't exist."));
        if (!requestRepository.existsParticipationRequestByRequesterAndEvent(userId, eventId)) {
            throw new IncorrectConditionException("Event id=" + eventId + " wasn't attended by User id= " + userId);
        }
        if (ratingRepository.existsByEventIdAndReviewerId(eventId, userId)) {
            throw new IncorrectConditionException("Event id=" + eventId + " was already reviewed by User id= " + userId);
        }
        final Rating rating = ratingMapper.toEntity(newRatingDto);
        rating.setReviewer(reviewer);
        rating.setEvent(event);
        return ratingMapper.toRatingDto(ratingRepository.save(rating));
    }

    public RatingDto getRatingById(Long userId, Long eventId, Long id) throws NotFoundException {
        final Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating with id=" + id + " doesn't exist."));
        return ratingMapper.toRatingDto(rating);
    }

    public List<RatingDto> getAllRatingsByEventId(Long userId, Long eventId) {
        return ratingRepository.getAllByReviewerIdAndEventId(userId, eventId)
                .stream()
                .map(ratingMapper::toRatingDto)
                .collect(Collectors.toList());
    }

    public void deleteRating(Long id) {
        ratingRepository.deleteById(id);
    }
}
