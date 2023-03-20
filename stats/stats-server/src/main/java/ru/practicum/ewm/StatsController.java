package ru.practicum.ewm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    public ResponseEntity<HitDto> saveRequest(@RequestBody HitDto hitDto) {
        log.info("post hit {}", hitDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(statsService.saveHit(hitDto));
    }

    @GetMapping("/stats")
    public List<ViewStats> getRequests(@RequestParam(value = "start") String start,
                                       @RequestParam(value = "end") String end,
                                       @RequestParam(value = "uris", required = false) List<String> uris,
                                       @RequestParam(value = "unique", defaultValue = "false", required = false) Boolean unique) {
        log.info("get stats start {}, end {}, uris {}, unique {}", start, end, uris, unique);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return statsService.getAllHits(LocalDateTime.parse(start, formatter), LocalDateTime.parse(end, formatter), uris, unique);
    }
}

