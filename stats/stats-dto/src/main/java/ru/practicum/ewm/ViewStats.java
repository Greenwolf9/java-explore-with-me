package ru.practicum.ewm;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.Value;

@Value
public class ViewStats {
    @NonNull
    @NotBlank
    String app;
    @NonNull
    @NotBlank
    String uri;
    Integer hits;
}
