package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("select case when count(c)> 0 then true else false end from Category c where lower(c.name) like lower(:name)")
    boolean existsNameLikeCustomQuery(@Param("name") String name);

    @Query("select case when count(e.id)> 0 then true else false end from Category c " +
            "left join Event as e on c.id = e.category.id where c.id = :catId")
    boolean existsCategoriesInEvents(@Param("catId") Long catId);
}
