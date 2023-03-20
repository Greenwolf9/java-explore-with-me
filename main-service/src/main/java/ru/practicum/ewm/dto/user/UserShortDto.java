package ru.practicum.ewm.dto.user;

import lombok.Setter;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Value
@Setter
public class UserShortDto {
    Long id;
    @NotEmpty(message = "Name is invalid. Please check.")
    @NotNull(message = "Name is invalid. Please check.")
    @NotBlank(message = "Name is invalid. Please check.")
    String name;
}
