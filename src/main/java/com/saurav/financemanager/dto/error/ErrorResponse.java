package com.saurav.financemanager.dto.error;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String message;

    private int status;

    private LocalDateTime timestamp;

    private List<String> errors;
}
