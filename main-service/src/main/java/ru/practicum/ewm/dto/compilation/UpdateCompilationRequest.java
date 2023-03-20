package ru.practicum.ewm.dto.compilation;

import lombok.Setter;
import lombok.Value;

import java.util.Set;

@Value
@Setter
public class UpdateCompilationRequest {
    Set<Long> events;
    boolean pinned;
    String title;
}
