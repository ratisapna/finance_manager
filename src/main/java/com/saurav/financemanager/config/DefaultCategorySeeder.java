package com.saurav.financemanager.config;

import com.saurav.financemanager.entity.Category;
import com.saurav.financemanager.enums.CategoryType;
import com.saurav.financemanager.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultCategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedCategory("Salary", CategoryType.INCOME);

        List.of("Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities")
                .forEach(name -> seedCategory(name, CategoryType.EXPENSE));
    }

    private void seedCategory(String name, CategoryType type) {
        if (categoryRepository.existsByNameAndUserIsNullAndDeletedFalse(name)) {
            return;
        }

        Category category = Category.builder()
                .name(name)
                .type(type)
                .custom(false)
                .deleted(false)
                .build();

        categoryRepository.save(category);
    }
}
