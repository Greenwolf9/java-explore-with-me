package ru.practicum.ewm.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.service.category.CategoryService;

import javax.validation.Valid;
import javax.validation.ValidationException;

@RequestMapping("/admin/categories")
@Slf4j
@RestController
public class CategoryAdminController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryDto> saveCategory(@RequestBody @Valid CategoryDto categoryDto, BindingResult bindingResult) throws DataIntegrityViolationException {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Field: name. Error: must not be blank. Value: null",
                    new Throwable("Incorrectly made request."));
        }
        log.info("Post category {} ", categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(categoryService.saveCategory(categoryDto));
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<CategoryDto> deleteCategory(@PathVariable("catId") Long catId) throws NotFoundException, DataIntegrityViolationException {
        log.info("Delete category by id {}", catId);
        categoryService.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable("catId") Long catId,
                                      @RequestBody @Valid CategoryDto categoryDto, BindingResult bindingResult) throws NotFoundException, DataIntegrityViolationException {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Request not valid");
        }
        log.info("Patch category by id {} category {} ", catId, categoryDto);
        return categoryService.updateCategory(catId, categoryDto);
    }
}
