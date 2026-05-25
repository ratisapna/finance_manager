package com.saurav.financemanager.repository;

import com.saurav.financemanager.entity.Category;
import com.saurav.financemanager.entity.Transaction;
import com.saurav.financemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByCategory(Category category);

    boolean existsByIdAndUser(Long id, User user);
}
