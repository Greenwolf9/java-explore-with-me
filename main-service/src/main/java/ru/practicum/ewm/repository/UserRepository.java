package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select case when count(u)> 0 then true else false end from User u where lower(u.name) like lower(:name)")
    boolean existsNameLikeCustomQuery(@Param("name") String name);

    @Query(value = "select u from User as u where u.id in (:ids)")
    Page<User> findAllById(@Param("ids") List<Long> ids, Pageable pageable);
}
