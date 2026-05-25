package com.saurav.financemanager.service;

import com.saurav.financemanager.dto.report.MonthlyReportResponse;
import com.saurav.financemanager.dto.report.YearlyReportResponse;
import com.saurav.financemanager.entity.User;
import com.saurav.financemanager.enums.CategoryType;
import com.saurav.financemanager.exception.BadRequestException;
import com.saurav.financemanager.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        validateMonth(month);
        User user = currentUserService.getCurrentUser();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Map<String, BigDecimal> income = totalsByCategory(user, startDate, endDate, CategoryType.INCOME);
        Map<String, BigDecimal> expenses = totalsByCategory(user, startDate, endDate, CategoryType.EXPENSE);

        return new MonthlyReportResponse(month, year, income, expenses, calculateNetSavings(income, expenses));
    }

    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(int year) {
        User user = currentUserService.getCurrentUser();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        Map<String, BigDecimal> income = totalsByCategory(user, startDate, endDate, CategoryType.INCOME);
        Map<String, BigDecimal> expenses = totalsByCategory(user, startDate, endDate, CategoryType.EXPENSE);

        return new YearlyReportResponse(year, income, expenses, calculateNetSavings(income, expenses));
    }

    private Map<String, BigDecimal> totalsByCategory(
            User user,
            LocalDate startDate,
            LocalDate endDate,
            CategoryType type
    ) {
        List<Object[]> rows = transactionRepository.sumByCategoryBetweenDatesAndType(user, startDate, endDate, type);
        Map<String, BigDecimal> totals = new LinkedHashMap<>();

        for (Object[] row : rows) {
            totals.put((String) row[0], (BigDecimal) row[1]);
        }

        return totals;
    }

    private BigDecimal calculateNetSavings(Map<String, BigDecimal> income, Map<String, BigDecimal> expenses) {
        BigDecimal totalIncome = income.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIncome.subtract(totalExpenses);
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
    }
}
