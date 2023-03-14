package ru.practicum.ewm.mappers;

import org.mapstruct.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {EventMapper.class, UserMapper.class, CategoryMapper.class})
public interface CompilationMapper {


    @Mapping(target = "events", expression = "java(map(newCompilationDto.getEvents()))")
    @Mapping(target = "id", ignore = true)
    Compilation mapToCompilation(NewCompilationDto newCompilationDto);

    @Mapping(source = "events", target = "events")
    CompilationDto mapToDto(Compilation compilation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "id", ignore = true)
    void updateCompilation(UpdateCompilationRequest updateCompilationRequest, @MappingTarget Compilation compilation);

    default List<Event> map(Set<Long> value) {
        List<Event> listOfEvents = new ArrayList<>(value.size());
        for (Long id : value) {
            Event event = new Event();
            event.setId(id);
            listOfEvents.add(event);
        }
        return listOfEvents;
    }

}
