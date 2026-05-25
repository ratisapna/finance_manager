package com.saurav.financemanager.dto.health;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HealthResponse {

    private String status;

    private String message;
}
