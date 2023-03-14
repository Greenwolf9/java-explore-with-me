package ru.practicum.ewm.service.category;

import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;

public interface CategoryService {
    CategoryDto saveCategory(CategoryDto categoryDto) throws DataIntegrityViolationException;

    void deleteCategory(Long catId) throws NotFoundException, DataIntegrityViolationException;

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto) throws NotFoundException, DataIntegrityViolationException;

    List<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getCategoryById(Long catId) throws NotFoundException;
}
