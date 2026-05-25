package com.saurav.financemanager.dto.report;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyReportResponse {

    private int month;

    private int year;

    private Map<String, BigDecimal> totalIncome;

    private Map<String, BigDecimal> totalExpenses;

    private BigDecimal netSavings;
}
