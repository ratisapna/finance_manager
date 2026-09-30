package com.ratisapna.financemanager.repository;

import com.ratisapna.financemanager.entity.SavingsGoal;
import com.ratisapna.financemanager.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserOrderByCreatedAtDesc(User user);

    Optional<SavingsGoal> findByIdAndUser(Long id, User user);
}
