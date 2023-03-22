package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.dto.user.UserShortDto;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    @NotNull
    String annotation;
    @NotNull
    CategoryDto category;
    Long confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    String createdOn;
    String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    String eventDate;
    Long id;
    @NotNull
    UserShortDto initiator;
    @NotNull
    Location location;
    @NotNull
    Boolean paid;
    int participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    String publishedOn;
    boolean requestModeration;
    String state;
    @NotNull
    String title;
    Long views;
    float rating;
}
