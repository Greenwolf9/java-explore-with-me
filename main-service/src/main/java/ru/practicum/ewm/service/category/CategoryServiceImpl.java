package ru.practicum.ewm.service.category;

import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.mappers.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {


    private final CategoryRepository categoryRepository;
    @Autowired
    protected CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDto saveCategory(CategoryDto categoryDto) throws DataIntegrityViolationException {
        final Category category = categoryMapper.mapToCategory(categoryDto);
        if (categoryRepository.existsNameLikeCustomQuery(category.getName())) {
            throw new DataIntegrityViolationException("Integrity constraint has been violated.");
        }
        return categoryMapper.mapToCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long catId) throws NotFoundException, DataIntegrityViolationException {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }
        if (categoryRepository.existsCategoriesInEvents(catId)) {
            throw new DataIntegrityViolationException("For the requested operation the conditions are not met.");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) throws NotFoundException, DataIntegrityViolationException {
        if (categoryRepository.existsNameLikeCustomQuery(categoryDto.getName())) {
            throw new DataIntegrityViolationException("Integrity constraint has been violated.");
        }
        final Category categoryToByUpdated = categoryRepository
                .findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        categoryToByUpdated.setName(categoryDto.getName());

        return categoryMapper.mapToCategoryDto(categoryRepository.save(categoryToByUpdated));
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        List<Category> categoryList = categoryRepository.findAll(PageRequest.of(from / size, size)).getContent();
        return categoryList.stream().map(categoryMapper::mapToCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) throws NotFoundException {
        final Category category = categoryRepository
                .findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
        return categoryMapper.mapToCategoryDto(category);
    }
}
