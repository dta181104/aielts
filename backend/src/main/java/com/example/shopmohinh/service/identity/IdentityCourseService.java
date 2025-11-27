package com.example.shopmohinh.service.identity;

import com.example.shopmohinh.dto.request.course.CourseRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.course.CourseResponse;
import com.example.shopmohinh.entity.course.CourseStatus;
import com.example.shopmohinh.entity.course.CourseType;
import com.example.shopmohinh.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityCourseService {

    private final CourseService courseService;

    public ApiResponse<Page<CourseResponse>> getAll(Pageable pageable) {
        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.getAllCourses(pageable))
                .build();
    }

    public ApiResponse<CourseResponse> getById(Long id) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.getById(id))
                .build();
    }

    public ApiResponse<Page<CourseResponse>> getByStatus(CourseStatus status, Pageable pageable) {
        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.getCoursesByStatus(status, pageable))
                .build();
    }

    public ApiResponse<Page<CourseResponse>> getByType(CourseType type, Pageable pageable) {
        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.getCoursesByType(type, pageable))
                .build();
    }

    public ApiResponse<Page<CourseResponse>> search(CourseStatus status, CourseType type, String keyword, Pageable pageable) {
        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.searchCourses(status, type, keyword, pageable))
                .build();
    }

    public ApiResponse<CourseResponse> create(CourseRequest request) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.create(request))
                .build();
    }

    public ApiResponse<CourseResponse> update(Long id, CourseRequest request) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.update(id, request))
                .build();
    }

    public ApiResponse<Void> delete(Long id) {
        courseService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}

