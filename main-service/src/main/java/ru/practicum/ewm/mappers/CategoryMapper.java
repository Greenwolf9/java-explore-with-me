package ru.practicum.ewm.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto mapToCategoryDto(Category category);

    @InheritInverseConfiguration
    Category mapToCategory(CategoryDto categoryDto);
}
