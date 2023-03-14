package ru.practicum.ewm.dto.compilation;

import lombok.Setter;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Value
@Setter
public class NewCompilationDto {
    Set<Long> events;
    boolean pinned;
    @NotNull
    @NotEmpty
    @NotBlank
    String title;
}
