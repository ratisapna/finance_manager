package com.saurav.financemanager.dto.goal;

import com.saurav.financemanager.entity.SavingsGoal;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoalResponse {

    private Long id;

    private String goalName;

    private BigDecimal targetAmount;

    private LocalDate targetDate;

    private LocalDate startDate;

    private BigDecimal currentProgress;

    private BigDecimal progressPercentage;

    private BigDecimal remainingAmount;

    public static GoalResponse from(
            SavingsGoal goal,
            BigDecimal currentProgress,
            BigDecimal progressPercentage,
            BigDecimal remainingAmount
    ) {
        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getTargetDate(),
                goal.getStartDate(),
                currentProgress,
                progressPercentage,
                remainingAmount
        );
    }
}
