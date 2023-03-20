package ru.practicum.ewm.service.compilation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mappers.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    @Autowired
    protected CompilationMapper compilationMapper;

    public CompilationServiceImpl(CompilationRepository compilationRepository, EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        final Compilation compilation = compilationMapper.mapToCompilation(newCompilationDto);
        compilation.setEvents(eventRepository.findAllById(newCompilationDto.getEvents()));
        return compilationMapper.mapToDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) throws NotFoundException {
        final Compilation compilationToBeUpdated = compilationRepository
                .findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compId + " was not found"));

        compilationMapper.updateCompilation(updateCompilationRequest, compilationToBeUpdated);
        List<Long> eventIds = new ArrayList<>();
        eventIds.addAll(updateCompilationRequest.getEvents());
        final List<Event> eventList = eventRepository.findAllById(eventIds);
        compilationToBeUpdated.setEvents(eventList);
        return compilationMapper.mapToDto(compilationRepository.save(compilationToBeUpdated));
    }

    @Override
    public void deleteCompilation(Long compId) throws NotFoundException {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id " + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto getCompilationById(Long compId) throws NotFoundException {
        final Compilation compilation = compilationRepository
                .findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compId + " was not found"));

        return compilationMapper.mapToDto(compilation);
    }

    @Override
    public List<CompilationDto> getListOfCompilations(boolean pinned, int from, int size) {
        List<Compilation> compilationList = compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size)).getContent();
        return compilationList.stream().map(compilationMapper::mapToDto).collect(Collectors.toList());
    }
}
