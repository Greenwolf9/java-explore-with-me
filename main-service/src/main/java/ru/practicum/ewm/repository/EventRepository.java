package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
    @Query(value = "select e from Event e where e.initiator.id = :userId ")
    List<Event> findAllByInitiatorId(@Param("userId") Long userId);

    @Query(value = "select e.* from events as e " +
            "where (:users is null or e.initiator_id IN (cast(:users as integer))) " +
            "and (:states is null or e.state IN (cast(:states as text)))" +
            "and (:categories is null OR e.category_id IN (cast(:categories as integer))) " +
            "and (cast(:rangeStart as date) is null or e.event_date > (cast(:rangeStart as date))) " +
            "and (cast(:rangeEnd as date) is null or e.event_date < (cast(:rangeEnd as date))) ", nativeQuery = true)
    Page<Event> findListOfEventByParameters(@Param("users") List<Long> users,
                                            @Param("states") List<String> states,
                                            @Param("categories") List<Long> categories,
                                            @Param("rangeStart") LocalDateTime rangeStart,
                                            @Param("rangeEnd") LocalDateTime rangeEnd,
                                            Pageable pageable);

    @Query(value = "select e.* from events as e " +
            "where (cast(:text as text) is null or e.annotation like :text " +
            "or cast(:text as text) is null or e.description like :text)" +
            "and (:categories is null OR e.category_id IN (:categories)) " +
            "and e.paid = cast(:paid as boolean) " +
            "and (cast(:rangeStart as date) is null or e.event_date > (cast(:rangeStart as date)))" +
            "and (cast(:rangeEnd as date) is null or e.event_date < (cast(:rangeEnd as date))) " +
            "or e.event_date > CURRENT_TIMESTAMP order by e.event_date desc", nativeQuery = true)
    Page<Event> findPublicEventsByParamsByEventDate(@Param("text") String text,
                                                    @Param("categories") List<Long> categories,
                                                    @Param("paid") boolean paid,
                                                    @Param("rangeStart") LocalDateTime rangeStart,
                                                    @Param("rangeEnd") LocalDateTime rangeEnd,
                                                    Pageable pageable);

    @Query(value = "select e.* from events as e " +
            "where (cast(:text as text) is null or e.annotation like :text " +
            "or cast(:text as text) is null or e.description like :text)" +
            "and (:categories is null OR e.category_id IN (cast(:categories as integer))) " +
            "and e.paid = cast(:paid as boolean) " +
            "and (cast(:rangeStart as date) is null or e.event_date > (cast(:rangeStart as date)))" +
            "and (cast(:rangeEnd as date) is null or e.event_date < (cast(:rangeEnd as date))) " +
            "or e.event_date > CURRENT_TIMESTAMP order by e.views desc", nativeQuery = true)
    Page<Event> findPublicEventsByParamsByViews(@Param("text") String text,
                                                    @Param("categories") List<Long> categories,
                                                    @Param("paid") boolean paid,
                                                    @Param("rangeStart") LocalDateTime rangeStart,
                                                    @Param("rangeEnd") LocalDateTime rangeEnd,
                                                    Pageable pageable);
}
