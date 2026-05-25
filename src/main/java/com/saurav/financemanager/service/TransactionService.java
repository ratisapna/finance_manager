package com.saurav.financemanager.service;

import com.saurav.financemanager.dto.common.MessageResponse;
import com.saurav.financemanager.dto.transaction.TransactionCreateRequest;
import com.saurav.financemanager.dto.transaction.TransactionListResponse;
import com.saurav.financemanager.dto.transaction.TransactionResponse;
import com.saurav.financemanager.dto.transaction.TransactionUpdateRequest;
import com.saurav.financemanager.entity.Category;
import com.saurav.financemanager.entity.Transaction;
import com.saurav.financemanager.entity.User;
import com.saurav.financemanager.enums.CategoryType;
import com.saurav.financemanager.exception.BadRequestException;
import com.saurav.financemanager.exception.NotFoundException;
import com.saurav.financemanager.repository.CategoryRepository;
import com.saurav.financemanager.repository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest request) {
        User user = currentUserService.getCurrentUser();
        Category category = categoryService.findAccessibleByName(request.getCategory(), user);

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .transactionDate(request.getDate())
                .category(category)
                .description(normalizeDescription(request.getDescription()))
                .user(user)
                .build();

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public TransactionListResponse getTransactions(
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            String categoryName,
            CategoryType type
    ) {
        User user = currentUserService.getCurrentUser();
        validateDateRange(startDate, endDate);
        Category category = resolveCategoryFilter(categoryId, categoryName, user);

        List<TransactionResponse> transactions = transactionRepository
                .findAll(
                        buildTransactionSpecification(user, startDate, endDate, category, type),
                        Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id"))
                )
                .stream()
                .map(TransactionResponse::from)
                .toList();

        return new TransactionListResponse(transactions);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request) {
        User user = currentUserService.getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }

        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            Category category = categoryService.findAccessibleByName(request.getCategory(), user);
            transaction.setCategory(category);
        }

        if (request.getDescription() != null) {
            transaction.setDescription(normalizeDescription(request.getDescription()));
        }

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public MessageResponse deleteTransaction(Long id) {
        User user = currentUserService.getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);

        return new MessageResponse("Transaction deleted successfully");
    }

    private Specification<Transaction> buildTransactionSpecification(
            User user,
            LocalDate startDate,
            LocalDate endDate,
            Category category,
            CategoryType type
    ) {
        return (root, query, criteriaBuilder) -> {
            root.fetch("category");

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user"), user));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }

            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("type"), type));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
    }

    private Category resolveCategoryFilter(Long categoryId, String categoryName, User user) {
        boolean hasCategoryName = categoryName != null && !categoryName.trim().isEmpty();

        if (categoryId != null && hasCategoryName) {
            throw new BadRequestException("Use either categoryId or category, not both");
        }

        if (categoryId != null) {
            return categoryRepository.findAccessibleById(categoryId, user)
                .orElseThrow(() -> new BadRequestException("Category not found"));
        }

        if (hasCategoryName) {
            return categoryService.findAccessibleByName(categoryName, user);
        }

        return null;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        return description.trim();
    }
}
