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

    @Query(value = "with oneStar as (select count(r.id)*1 as one from ratings as r where r.event_id = :eventId and r.stars = 1), " +
            "twoStar as (select count(r.id)*2 as two from ratings as r where r.event_id = :eventId and r.stars = 2)," +
            "threeStar as (select count(r.id)*3 as three from ratings as r where r.event_id = :eventId and r.stars = 3)," +
            "fourStar as (select count(r.id)*4 as four from ratings as r where r.event_id = :eventId and r.stars = 4)," +
            "fiveStar as (select count(r.id)*5 as five from ratings as r where r.event_id = :eventId and r.stars = 5) " +
            "select  (q1.one +q2.two + q3.three +q4.four + q5.five) as score, count(r.id) as responses " +
            "from oneStar q1, twoStar q2, threeStar q3, fourStar q4, fiveStar q5, ratings r " +
            "where r.event_id = :eventId group by score ", nativeQuery = true)
    Score totalScore(@Param("eventId") Long eventId);

}
