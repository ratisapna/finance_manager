package com.saurav.financemanager.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.saurav.financemanager.entity.Category;
import com.saurav.financemanager.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse {

    private Long id;

    private String name;

    private CategoryType type;

    @JsonProperty("isCustom")
    private boolean isCustom;

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.isCustom()
        );
    }
}
