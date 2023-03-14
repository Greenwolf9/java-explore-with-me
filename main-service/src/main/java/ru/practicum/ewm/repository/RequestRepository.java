package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.Status;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    @Query("SELECT count(p) FROM ParticipationRequest p WHERE p.event.id = :eventId and p.status = :status ")
    int countByStatus(@Param("eventId") Long eventId, @Param("status") Status status);

    @Query("select case when count(p)> 0 then true else false end from ParticipationRequest p where p.requester.id = :userId " +
            " and p.event.id = :eventId ")
    boolean existsParticipationRequestByRequesterAndEvent(@Param("userId") Long userId, @Param("eventId") Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    @Query(value = "select r from ParticipationRequest as r where r.id in (:ids) ")
    List<ParticipationRequest> findAllByListOfIds(@Param("ids") List<Long> ids);

    @Query(value = "select r from ParticipationRequest  as r where r.requester.id <> :userId and r.event.id = (:eventId)")
    List<ParticipationRequest> findAllByEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query(value = "select r from ParticipationRequest as r where r.event.id = :eventId and r.status = :status ")
    List<ParticipationRequest> findAllByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") Status status);
}
