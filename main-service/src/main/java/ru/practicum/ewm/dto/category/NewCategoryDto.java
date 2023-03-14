package ru.practicum.ewm.dto.category;

import lombok.Setter;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Value
@Setter
public class NewCategoryDto {
    @NotNull
    @NotEmpty
    @NotBlank
    String name;
}
