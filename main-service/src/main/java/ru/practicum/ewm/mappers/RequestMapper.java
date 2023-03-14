package ru.practicum.ewm.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.ParticipationRequest;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface RequestMapper {
    @Mapping(source = "created", target = "created")
    @Mapping(source = "event", target = "event.id")
    @Mapping(source = "requester", target = "requester.id")
    @Mapping(source = "status", target = "status")
    ParticipationRequest mapToRequest(ParticipationRequestDto participationRequestDto);

    @InheritInverseConfiguration
    ParticipationRequestDto mapRequestToDto(ParticipationRequest participationRequest);
}
