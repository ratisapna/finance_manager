package com.saurav.financemanager.repository;

import com.saurav.financemanager.entity.SavingsGoal;
import com.saurav.financemanager.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserOrderByCreatedAtDesc(User user);

    Optional<SavingsGoal> findByIdAndUser(Long id, User user);
}
