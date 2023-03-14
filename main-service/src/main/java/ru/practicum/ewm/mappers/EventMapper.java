package ru.practicum.ewm.mappers;

import org.mapstruct.*;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.State;
import ru.practicum.ewm.model.StateAction;


@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class, JsonNullableMapper.class})
public interface EventMapper {

    @Mapping(target = "createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventFullDto eventToFullDto(Event event);

    @Mapping(source = "category", target = "category.id")
    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Event mapToEvent(NewEventDto newEventDto);

    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventShortDto eventToShortDto(Event event);


    @BeforeMapping
    default void stateOfEvent(UpdateEventUserRequest update, @MappingTarget Event destination) {
        switch (StateAction.valueOf(update.getStateAction())) {
            case CANCEL_REVIEW:
                destination.setState(State.CANCELED);
                break;
            case SEND_TO_REVIEW:
                destination.setState(State.PENDING);
                break;
        }
    }

    @Mapping(source = "category", target = "category.id")
    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "paid", ignore = true)
    @Mapping(target = "participantLimit", ignore = true)
    @Mapping(target = "requestModeration", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void update(UpdateEventUserRequest update, @MappingTarget Event destination);

    @BeforeMapping
    default void stateOfEvent(UpdateEventAdminRequest update, @MappingTarget Event destination) {
        switch (StateAction.valueOf(update.getStateAction())) {
            case PUBLISH_EVENT:
                destination.setState(State.PUBLISHED);
                break;
            case REJECT_EVENT:
                destination.setState(State.REJECTED);
                break;
        }
    }

    @Mapping(source = "category", target = "category.id")
    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "paid", ignore = true)
    @Mapping(target = "participantLimit", ignore = true)
    @Mapping(target = "requestModeration", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void update(UpdateEventAdminRequest update, @MappingTarget Event destination);

}
