package ru.practicum.ewm.dto.rating;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class NewRatingDto {
    @NotNull
    int stars;
    String comments;
}
