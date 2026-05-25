package com.saurav.financemanager.service;

import com.saurav.financemanager.dto.common.MessageResponse;
import com.saurav.financemanager.dto.goal.GoalCreateRequest;
import com.saurav.financemanager.dto.goal.GoalListResponse;
import com.saurav.financemanager.dto.goal.GoalResponse;
import com.saurav.financemanager.dto.goal.GoalUpdateRequest;
import com.saurav.financemanager.entity.SavingsGoal;
import com.saurav.financemanager.entity.User;
import com.saurav.financemanager.enums.CategoryType;
import com.saurav.financemanager.exception.NotFoundException;
import com.saurav.financemanager.repository.SavingsGoalRepository;
import com.saurav.financemanager.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public GoalResponse createGoal(GoalCreateRequest request) {
        User user = currentUserService.getCurrentUser();

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName().trim())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(request.getStartDate() == null ? LocalDate.now() : request.getStartDate())
                .user(user)
                .build();

        return buildResponse(savingsGoalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public GoalListResponse getGoals() {
        User user = currentUserService.getCurrentUser();
        List<GoalResponse> goals = savingsGoalRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::buildResponse)
                .toList();

        return new GoalListResponse(goals);
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoal(Long id) {
        User user = currentUserService.getCurrentUser();
        SavingsGoal goal = savingsGoalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Goal not found"));

        return buildResponse(goal);
    }

    @Transactional
    public GoalResponse updateGoal(Long id, GoalUpdateRequest request) {
        User user = currentUserService.getCurrentUser();
        SavingsGoal goal = savingsGoalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Goal not found"));

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }

        return buildResponse(savingsGoalRepository.save(goal));
    }

    @Transactional
    public MessageResponse deleteGoal(Long id) {
        User user = currentUserService.getCurrentUser();
        SavingsGoal goal = savingsGoalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Goal not found"));

        savingsGoalRepository.delete(goal);

        return new MessageResponse("Goal deleted successfully");
    }

    private GoalResponse buildResponse(SavingsGoal goal) {
        BigDecimal currentProgress = calculateProgress(goal);
        BigDecimal percentage = currentProgress
                .multiply(ONE_HUNDRED)
                .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentProgress).max(BigDecimal.ZERO);

        return GoalResponse.from(goal, currentProgress, percentage, remainingAmount);
    }

    private BigDecimal calculateProgress(SavingsGoal goal) {
        BigDecimal income = transactionRepository.sumAmountFromDateByType(
                goal.getUser(),
                goal.getStartDate(),
                CategoryType.INCOME
        );
        BigDecimal expenses = transactionRepository.sumAmountFromDateByType(
                goal.getUser(),
                goal.getStartDate(),
                CategoryType.EXPENSE
        );

        return income.subtract(expenses);
    }
}
