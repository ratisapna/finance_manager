package com.saurav.financemanager.dto.goal;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoalListResponse {

    private List<GoalResponse> goals;
}
