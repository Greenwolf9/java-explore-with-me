package ru.practicum.ewm.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.rating.NewRatingDto;
import ru.practicum.ewm.dto.rating.RatingDto;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Rating;
import ru.practicum.ewm.model.User;

@Mapper(componentModel = "spring", uses = {User.class, Event.class})
public interface RatingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "event", ignore = true)
    Rating toEntity(NewRatingDto newRatingDto);

    @Mapping(source = "reviewer.id", target = "reviewerId")
    @Mapping(source = "event.id", target = "eventId")
    RatingDto toRatingDto(Rating rating);
}
