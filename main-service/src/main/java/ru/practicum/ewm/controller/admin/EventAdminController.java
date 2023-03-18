package ru.practicum.ewm.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.service.event.EventService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/events")
public class EventAdminController {

    private final EventService eventService;

    @Autowired
    public EventAdminController(EventService eventService) {
        this.eventService = eventService;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable("eventId") Long eventId,
                                           @RequestBody UpdateEventAdminRequest updateEventAdminRequest)
            throws NotFoundException, IncorrectConditionException {
        log.info("Patch event {} by updateEventAdminRequest {} ", eventId, updateEventAdminRequest);
        return eventService.updateEventByAdmin(eventId, updateEventAdminRequest);
    }

    @GetMapping
    public List<EventFullDto> findListOfEventByParameters(@RequestParam(value = "users", required = false) List<Long> users,
                                                          @RequestParam(value = "states", required = false) List<String> states,
                                                          @RequestParam(value = "categories", required = false) List<Long> categories,
                                                          @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                                          @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                                          @RequestParam(value = "from", defaultValue = "0") int from,
                                                          @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("Get (admin) events by parameters");
        return eventService.findListOfEventByParameters(users, states, categories, rangeStart, rangeEnd, from, size);

    }
}
