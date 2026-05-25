package com.saurav.financemanager.repository;

import com.saurav.financemanager.entity.Category;
import com.saurav.financemanager.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameAndUserIsNullAndDeletedFalse(String name);

    boolean existsByNameAndUserAndDeletedFalse(String name, User user);

    List<Category> findByUserIsNullAndDeletedFalseOrderByNameAsc();

    List<Category> findByUserAndDeletedFalseOrderByNameAsc(User user);

    Optional<Category> findByNameAndUserIsNullAndDeletedFalse(String name);

    Optional<Category> findByNameAndUserAndDeletedFalse(String name, User user);

    @Query("""
            select c from Category c
            where c.id = :id
              and c.deleted = false
              and (c.user is null or c.user = :user)
            """)
    Optional<Category> findAccessibleById(@Param("id") Long id, @Param("user") User user);

    @Query("""
            select c from Category c
            where lower(c.name) = lower(:name)
              and c.deleted = false
              and (c.user is null or c.user = :user)
            order by c.custom desc
            """)
    List<Category> findAccessibleByName(@Param("name") String name, @Param("user") User user);
}
