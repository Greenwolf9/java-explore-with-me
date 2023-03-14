package ru.practicum.ewm.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.service.compilation.CompilationService;

import javax.validation.Valid;
import javax.validation.ValidationException;

@RestController
@Slf4j
@RequestMapping("/admin/compilations")
public class CompilationAdminController {

    private final CompilationService compilationService;

    @Autowired
    public CompilationAdminController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    public ResponseEntity<CompilationDto> saveCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Incorrectly made request.");
        }
        log.info("Post compilation {} ", newCompilationDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilationService.saveCompilation(newCompilationDto));
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<CompilationDto> deleteCompilation(@PathVariable("compId") Long compId) throws NotFoundException {
        log.info("Delete compilation by id {} ", compId);
        compilationService.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable("compId") Long compId,
                                            @RequestBody UpdateCompilationRequest updateCompilationRequest) throws NotFoundException {
        return compilationService.updateCompilation(compId, updateCompilationRequest);
    }
}
