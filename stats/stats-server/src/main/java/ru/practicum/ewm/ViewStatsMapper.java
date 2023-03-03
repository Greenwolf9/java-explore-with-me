package ru.practicum.ewm;

public class ViewStatsMapper {
    public static ViewStats mapToViewStats(Stat stat) {
        return new ViewStats(stat.getApp(), stat.getUri(), stat.getHits());
    }
}
