package com.saurav.financemanager.controller;

import com.saurav.financemanager.dto.category.CategoryListResponse;
import com.saurav.financemanager.dto.category.CategoryRequest;
import com.saurav.financemanager.dto.category.CategoryResponse;
import com.saurav.financemanager.dto.common.MessageResponse;
import com.saurav.financemanager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<CategoryListResponse> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.deleteCategory(name));
    }
}
