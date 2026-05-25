package com.saurav.financemanager.repository;

import com.saurav.financemanager.entity.Category;
import com.saurav.financemanager.entity.Transaction;
import com.saurav.financemanager.entity.User;
import com.saurav.financemanager.enums.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    boolean existsByCategory(Category category);

    boolean existsByIdAndUser(Long id, User user);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            join t.category c
            where t.user = :user
              and t.transactionDate >= :startDate
              and c.type = :type
            """)
    BigDecimal sumAmountFromDateByType(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("type") CategoryType type
    );

    @Query("""
            select c.name, coalesce(sum(t.amount), 0)
            from Transaction t
            join t.category c
            where t.user = :user
              and t.transactionDate between :startDate and :endDate
              and c.type = :type
            group by c.name
            order by c.name
            """)
    List<Object[]> sumByCategoryBetweenDatesAndType(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") CategoryType type
    );
}
