package com.saurav.financemanager.dto.transaction;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransactionListResponse {

    private List<TransactionResponse> transactions;
}
