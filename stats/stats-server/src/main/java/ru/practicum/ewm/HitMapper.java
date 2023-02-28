package ru.practicum.ewm;

import ru.practicum.ewm.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HitMapper {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static HitDto mapToHitDto(Hit hit) {
        return new HitDto(hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp().format(formatter));
    }

    public static Hit mapToHit(HitDto hitDto) {
        LocalDateTime dateTime = LocalDateTime.parse(hitDto.getTimestamp(), formatter);
        Hit hit = new Hit();
        hit.setApp(hitDto.getApp());
        hit.setUri(hitDto.getUri());
        hit.setIp(hitDto.getIp());
        hit.setTimestamp(dateTime);
        return hit;
    }

    public static List<HitDto> toHitDtoList(Collection<Hit> hits) {
        return hits.stream()
                .map(HitMapper::mapToHitDto)
                .collect(Collectors.toList());
    }
}
