package com.saurav.financemanager.dto.category;

import com.saurav.financemanager.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 80, message = "Category name must not exceed 80 characters")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType type;
}
