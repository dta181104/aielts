package com.example.shopmohinh.controller.course;

import com.example.shopmohinh.dto.request.course.CourseRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.course.CourseResponse;
import com.example.shopmohinh.entity.course.CourseStatus;
import com.example.shopmohinh.entity.course.CourseType;
import com.example.shopmohinh.service.course.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ApiResponse<CourseResponse> create(@RequestBody @Valid CourseRequest request) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<CourseResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.getAllCourses(pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getById(@PathVariable Long id) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.getById(id))
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<Page<CourseResponse>> getByStatus(
            @PathVariable CourseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.getCoursesByStatus(status, pageable))
                .build();
    }

    @GetMapping("/type/{type}")
    public ApiResponse<Page<CourseResponse>> getByType(
            @PathVariable CourseType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.getCoursesByType(type, pageable))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<CourseResponse>> search(
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) CourseType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.searchCourses(status, type, keyword, pageable))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid CourseRequest request) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Course deleted successfully")
                .build();
    }
}

