package ru.practicum.ewm.dto.rating;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class RatingDto {
    Long id;
    @NotNull
    int stars;
    String comments;
    Long reviewerId;
    Long eventId;
}
