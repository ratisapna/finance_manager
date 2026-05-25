package com.saurav.financemanager.repository;

import com.saurav.financemanager.entity.Category;
import com.saurav.financemanager.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameAndUserIsNullAndDeletedFalse(String name);

    boolean existsByNameAndUserAndDeletedFalse(String name, User user);

    List<Category> findByUserIsNullAndDeletedFalseOrderByNameAsc();

    List<Category> findByUserAndDeletedFalseOrderByNameAsc(User user);

    Optional<Category> findByNameAndUserIsNullAndDeletedFalse(String name);

    Optional<Category> findByNameAndUserAndDeletedFalse(String name, User user);
}
