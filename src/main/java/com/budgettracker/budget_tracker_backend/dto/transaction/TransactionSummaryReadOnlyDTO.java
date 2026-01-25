package com.budgettracker.budget_tracker_backend.dto.transaction;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record TransactionSummaryReadOnlyDTO(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        BigDecimal currentMonthIncome,
        BigDecimal currentMonthExpenses,
        BigDecimal currentMonthBalance,
        BigDecimal currentYearSavings,
        BigDecimal currentYearSavingsRate,
        BigDecimal twelveMonthAverageExpense,
        List<TransactionReadOnlyDTO> recentTransactions,
        Map<String, BigDecimal> topSpendingCategories,
        LocalDateTime generatedAt
) {}