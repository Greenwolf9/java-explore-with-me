package ru.practicum.ewm.dto.event;

import lombok.Setter;
import lombok.Value;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import javax.validation.constraints.NotNull;

@Value
@Setter
public class EventShortDto {
    @NotNull
    String annotation;
    @NotNull
    CategoryDto category;
    Long confirmedRequests;
    @NotNull
    String eventDate;
    Long id;
    @NotNull
    UserShortDto initiator;
    @NotNull
    boolean paid;
    @NotNull
    String title;
    Long views;
}
