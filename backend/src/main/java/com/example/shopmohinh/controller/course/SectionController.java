package com.example.shopmohinh.controller.course;

import com.example.shopmohinh.dto.request.course.SectionRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.course.SectionResponse;
import com.example.shopmohinh.service.course.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
@Slf4j
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    public ApiResponse<SectionResponse> create(@RequestBody @Valid SectionRequest request) {
        return ApiResponse.<SectionResponse>builder()
                .result(sectionService.create(request))
                .build();
    }

    @GetMapping("/course/{courseId}")
    public ApiResponse<List<SectionResponse>> getByCourseId(@PathVariable Long courseId) {
        return ApiResponse.<List<SectionResponse>>builder()
                .result(sectionService.getByCourseId(courseId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SectionResponse> getById(@PathVariable Long id) {
        return ApiResponse.<SectionResponse>builder()
                .result(sectionService.getById(id))
                .build();
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<SectionResponse>> getByCategoryId(@PathVariable Long categoryId) {
        return ApiResponse.<List<SectionResponse>>builder()
                .result(sectionService.getByCategoryId(categoryId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<SectionResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid SectionRequest request) {
        return ApiResponse.<SectionResponse>builder()
                .result(sectionService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sectionService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Section deleted successfully")
                .build();
    }
}

