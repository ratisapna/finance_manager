package com.saurav.financemanager.dto.transaction;

import com.saurav.financemanager.entity.Transaction;
import com.saurav.financemanager.enums.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransactionResponse {

    private Long id;

    private BigDecimal amount;

    private LocalDate date;

    private String category;

    private String description;

    private CategoryType type;

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getCategory().getName(),
                transaction.getDescription(),
                transaction.getCategory().getType()
        );
    }
}
