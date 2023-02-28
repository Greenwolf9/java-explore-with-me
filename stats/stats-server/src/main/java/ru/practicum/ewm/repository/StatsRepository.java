package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.Stat;
import ru.practicum.ewm.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query(value = "SELECT distinct h.ip, h.app as app, h.uri as uri, count(h.id) as hits " +
            "from hit as h " +
            "where h.timestamp between :start and :end " +
            "and h.uri in :uris group by app, uri ", nativeQuery = true)
    List<Stat> findAllByIpUnique(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("uris") List<String> uris);

    @Query(value = "SELECT h.app as app, h.uri as uri, count(h.id) as hits " +
            "from Hit as h " +
            "where h.timestamp between :start and :end " +
            "and h.uri in :uris group by app, uri ")
    List<Stat> findAllByIpNotUnique(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("uris") List<String> uris);
}
