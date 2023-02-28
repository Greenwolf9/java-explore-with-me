package ru.practicum.ewm.service;

import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    HitDto saveHit(HitDto hitDto);

    List<ViewStats> getAllHits(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
