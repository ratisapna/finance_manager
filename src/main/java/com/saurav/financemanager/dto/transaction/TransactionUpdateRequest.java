package com.saurav.financemanager.dto.transaction;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionUpdateRequest {

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Size(max = 80, message = "Category must not exceed 80 characters")
    private String category;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
