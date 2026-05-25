package com.saurav.financemanager.dto.category;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryListResponse {

    private List<CategoryResponse> categories;
}
