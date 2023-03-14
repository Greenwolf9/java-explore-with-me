package ru.practicum.ewm;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ViewStats {
    @NonNull
    @NotBlank
    String app;
    @NonNull
    @NotBlank
    String uri;
    Integer hits;
}
