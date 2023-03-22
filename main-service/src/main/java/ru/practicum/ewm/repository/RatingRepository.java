package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.rating.Score;
import ru.practicum.ewm.model.Rating;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> getAllByReviewerIdAndEventId(Long userId, Long eventId);

    @Query(value = "select case when count(r)> 0 then true else false end from Rating r where " +
            "r.event.id = :eventId and r.reviewer.id = :userId")
    boolean existsByEventIdAndReviewerId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    @Query(value = "select case when count(r)> 0 then true else false end from Rating r where " +
            "r.event.id = :eventId ")
    boolean existsByEventId(@Param("eventId") Long eventId);

    @Query(value = "select ((select count(r.id) as one from ratings as r where r.event_id = :eventId and r.stars = 1)*1 + " +
            " (select count(r.id) as two from ratings as r where r.event_id = :eventId and r.stars = 2)*2 + " +
            "(select count(r.id) as three from ratings as r where r.event_id = :eventId and r.stars = 3)*3 + " +
            "(select count(r.id) as four from ratings as r where r.event_id = :eventId and r.stars = 4)*4 + " +
            "(select count(r.id) as five from ratings as r where r.event_id = :eventId and r.stars = 5)*5) as score, count(r.id) as responses, r.event_id as eventId " +
            "from ratings as r where r.event_id = :eventId group by r.event_id ", nativeQuery = true)
    Score totalScore(@Param("eventId") Long eventId);

}
