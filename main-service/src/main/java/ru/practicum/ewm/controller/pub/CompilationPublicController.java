package ru.practicum.ewm.controller.pub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.service.compilation.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/compilations")
public class CompilationPublicController {

    private final CompilationService compilationService;

    @Autowired
    public CompilationPublicController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable("compId") Long compId) throws NotFoundException {
        log.info("Get compilation by id {}", compId);
        return compilationService.getCompilationById(compId);
    }

    @GetMapping
    public List<CompilationDto> getListOfCompilations(@RequestParam(value = "pinned", defaultValue = "false") boolean pinned,
                                                      @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
                                                      @Positive @RequestParam(value = "size", defaultValue = "10") int size) {

        return compilationService.getListOfCompilations(pinned, from, size);
    }
}
