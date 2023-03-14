package ru.practicum.ewm.service.compilation;

import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;

public interface CompilationService {

    CompilationDto saveCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) throws NotFoundException;

    void deleteCompilation(Long compId) throws NotFoundException;

    CompilationDto getCompilationById(Long compId) throws NotFoundException;

    List<CompilationDto> getListOfCompilations(boolean pinned, int from, int size);
}
