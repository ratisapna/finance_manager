package com.saurav.financemanager.service;

import com.saurav.financemanager.dto.category.CategoryListResponse;
import com.saurav.financemanager.dto.category.CategoryRequest;
import com.saurav.financemanager.dto.category.CategoryResponse;
import com.saurav.financemanager.dto.common.MessageResponse;
import com.saurav.financemanager.entity.Category;
import com.saurav.financemanager.entity.User;
import com.saurav.financemanager.exception.BadRequestException;
import com.saurav.financemanager.exception.ConflictException;
import com.saurav.financemanager.exception.ForbiddenException;
import com.saurav.financemanager.exception.NotFoundException;
import com.saurav.financemanager.repository.CategoryRepository;
import com.saurav.financemanager.repository.TransactionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public CategoryListResponse getCategories() {
        User user = currentUserService.getCurrentUser();
        List<Category> categories = new ArrayList<>();
        categories.addAll(categoryRepository.findByUserIsNullAndDeletedFalseOrderByNameAsc());
        categories.addAll(categoryRepository.findByUserAndDeletedFalseOrderByNameAsc(user));

        List<CategoryResponse> responses = categories.stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(CategoryResponse::from)
                .toList();

        return new CategoryListResponse(responses);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = currentUserService.getCurrentUser();
        String name = normalizeName(request.getName());

        if (categoryRepository.existsByNameAndUserAndDeletedFalse(name, user)) {
            throw new ConflictException("Category already exists");
        }

        Category category = Category.builder()
                .name(name)
                .type(request.getType())
                .custom(true)
                .deleted(false)
                .user(user)
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public MessageResponse deleteCategory(String name) {
        User user = currentUserService.getCurrentUser();
        String normalizedName = normalizeName(name);

        if (categoryRepository.findByNameAndUserIsNullAndDeletedFalse(normalizedName).isPresent()) {
            throw new ForbiddenException("Default categories cannot be deleted");
        }

        Category category = categoryRepository.findByNameAndUserAndDeletedFalse(normalizedName, user)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (transactionRepository.existsByCategory(category)) {
            throw new BadRequestException("Category is currently referenced by transactions");
        }

        category.setDeleted(true);

        return new MessageResponse("Category deleted successfully");
    }

    public Category findAccessibleByName(String name, User user) {
        return categoryRepository.findAccessibleByName(normalizeName(name), user)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Category not found"));
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Category name is required");
        }

        return name.trim();
    }
}
