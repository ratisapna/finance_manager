package com.saurav.financemanager.repository;

import com.saurav.financemanager.entity.Category;
import com.saurav.financemanager.entity.Transaction;
import com.saurav.financemanager.entity.User;
import com.saurav.financemanager.enums.CategoryType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByCategory(Category category);

    boolean existsByIdAndUser(Long id, User user);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    @Query("""
            select t from Transaction t
            join fetch t.category c
            where t.user = :user
              and (:startDate is null or t.transactionDate >= :startDate)
              and (:endDate is null or t.transactionDate <= :endDate)
              and (:categoryId is null or c.id = :categoryId)
              and (:type is null or c.type = :type)
            order by t.transactionDate desc, t.id desc
            """)
    List<Transaction> findTransactions(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId,
            @Param("type") CategoryType type
    );
}
