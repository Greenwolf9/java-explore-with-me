package ru.practicum.ewm.dto.compilation;

import lombok.Setter;
import lombok.Value;
import ru.practicum.ewm.dto.event.EventShortDto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@Setter
public class CompilationDto {
    List<EventShortDto> events;
    @NotNull
    Long id;
    @NotNull
    boolean pinned;
    @NotNull
    @NotEmpty
    String title;
}
