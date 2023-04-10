package ru.practicum.ewm.dto.event;

import lombok.Value;
import ru.practicum.ewm.model.Location;

@Value
public class UpdateEventUserRequest {

    String annotation;
    Long category;
    String description;
    String eventDate;
    Location location;
    boolean paid;
    int participantLimit;
    boolean requestModeration;
    String stateAction;
    String title;
}
