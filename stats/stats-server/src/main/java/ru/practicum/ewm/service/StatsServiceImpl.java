package ru.practicum.ewm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.HitMapper;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.model.Hit;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {
    @Autowired
    private StatsRepository statsRepository;

    @Override
    public HitDto saveHit(HitDto hitDto) {
        final Hit hit = HitMapper.mapToHit(hitDto);
        return HitMapper.mapToHitDto(statsRepository.save(hit));
    }

    @Override
    public List<ViewStats> getAllHits(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            return statsRepository.findAllByIpUnique(start, end, uris)
                    .stream()
                    .map(dto -> new ViewStats(dto.getApp(), dto.getUri(), dto.getHits()))
                    .sorted((dto1, dto2) -> Integer.compare(dto2.getHits(), dto1.getHits()))
                    .collect(Collectors.toList());
        } else {
            return statsRepository.findAllByIpNotUnique(start, end, uris)
                    .stream()
                    .map(dto -> new ViewStats(dto.getApp(), dto.getUri(), dto.getHits()))
                    .sorted((dto1, dto2) -> Integer.compare(dto2.getHits(), dto1.getHits()))
                    .collect(Collectors.toList());
        }
    }
}
