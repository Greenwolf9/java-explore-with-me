package ru.practicum.ewm.dto.event;

import lombok.Setter;
import lombok.Value;
import ru.practicum.ewm.model.Location;

@Value
@Setter
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
