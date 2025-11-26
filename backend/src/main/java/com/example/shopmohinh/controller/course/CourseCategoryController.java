package com.example.shopmohinh.controller.course;

import com.example.shopmohinh.dto.request.course.CourseCategoryRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.course.CourseCategoryResponse;
import com.example.shopmohinh.service.course.CourseCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course-categories")
@RequiredArgsConstructor
@Slf4j
public class CourseCategoryController {

    private final CourseCategoryService categoryService;

    @PostMapping
    public ApiResponse<CourseCategoryResponse> create(@RequestBody @Valid CourseCategoryRequest request) {
        return ApiResponse.<CourseCategoryResponse>builder()
                .result(categoryService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<CourseCategoryResponse>> getAll() {
        return ApiResponse.<List<CourseCategoryResponse>>builder()
                .result(categoryService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseCategoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.<CourseCategoryResponse>builder()
                .result(categoryService.getById(id))
                .build();
    }

    @GetMapping("/code/{code}")
    public ApiResponse<CourseCategoryResponse> getByCode(@PathVariable String code) {
        return ApiResponse.<CourseCategoryResponse>builder()
                .result(categoryService.getByCode(code))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseCategoryResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid CourseCategoryRequest request) {
        return ApiResponse.<CourseCategoryResponse>builder()
                .result(categoryService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Category deleted successfully")
                .build();
    }
}

