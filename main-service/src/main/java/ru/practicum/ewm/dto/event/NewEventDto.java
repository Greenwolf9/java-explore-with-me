package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Setter;
import lombok.Value;
import ru.practicum.ewm.model.Location;

import javax.validation.constraints.*;

@Value
@Setter
public class NewEventDto {
    @NotNull
    @Size(min = 20, max = 2000)
    String annotation;
    @NotNull
    Long category;
    @Size(min = 20, max = 7000)
    String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    String eventDate;
    @NotNull
    Location location;
    boolean paid;
    int participantLimit;
    boolean requestModeration;
    @Size(min = 3, max = 120)
    String title;
}
