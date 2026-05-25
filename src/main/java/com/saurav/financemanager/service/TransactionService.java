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
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
            CategoryType type
    ) {
        User user = currentUserService.getCurrentUser();
        validateDateRange(startDate, endDate);
        validateCategoryAccess(categoryId, user);

        List<TransactionResponse> transactions = transactionRepository
                .findTransactions(user, startDate, endDate, categoryId, type)
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

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
    }

    private void validateCategoryAccess(Long categoryId, User user) {
        if (categoryId == null) {
            return;
        }

        categoryRepository.findAccessibleById(categoryId, user)
                .orElseThrow(() -> new BadRequestException("Category not found"));
    }

    private String normalizeDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        return description.trim();
    }
}
