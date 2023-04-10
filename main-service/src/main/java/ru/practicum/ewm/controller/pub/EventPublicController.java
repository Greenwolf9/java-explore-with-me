package ru.practicum.ewm.controller.pub;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.service.event.EventService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/events")
public class EventPublicController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @Autowired
    public EventPublicController(EventService eventService, StatsClient statsClient) {
        this.eventService = eventService;
        this.statsClient = statsClient;
    }

    @GetMapping("/{id}")
    public EventFullDto getFullEvent(@PathVariable("id") Long id, HttpServletRequest request) throws NotFoundException, DataIntegrityViolationException, IOException, InterruptedException {
        log.info("Get full event info by id{} ", id);
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());
        statsClient.hit(request);
        return eventService.getEventById(id);
    }

    @GetMapping
    public List<EventShortDto> getPublicListOfEventWithParams(@RequestParam(value = "text", required = false) String text,
                                                              @RequestParam(value = "categories", required = false) List<Long> categories,
                                                              @RequestParam(value = "paid", required = false) boolean paid,
                                                              @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                                              @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                                              @RequestParam(value = "onlyAvailable", defaultValue = "false") boolean onlyAvailable,
                                                              @RequestParam(value = "sort", defaultValue = "EVENT_DATE") String sort,
                                                              @RequestParam(value = "from", defaultValue = "0") int from,
                                                              @RequestParam(value = "size", defaultValue = "10") int size,
                                                              HttpServletRequest request) {
        log.info("Get full public events info by params ");
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());
        statsClient.hit(request);
        return eventService.findFilteredEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/popular")
    public List<EventShortDto> getMostPopularEvents(@RequestParam(value = "from", defaultValue = "0") int from,
                                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                                    HttpServletRequest request) {
        log.info("Get most popular events info by params ");
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());
        statsClient.hit(request);
        return eventService.findMostPopularEvents(from, size);
    }
}

