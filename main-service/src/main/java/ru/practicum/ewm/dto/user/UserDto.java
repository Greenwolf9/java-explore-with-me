package ru.practicum.ewm.dto.user;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Value
@Setter
public class UserDto {
    @Email(message = "Email is invalid. Please check!")
    @NotEmpty
    @NotNull
    String email;
    Long id;
    @NotBlank(message = "Name is invalid. Please check.")
    @NotEmpty(message = "Name is invalid. Please check.")
    @NotNull(message = "Name is invalid. Please check.")
    String name;
}
