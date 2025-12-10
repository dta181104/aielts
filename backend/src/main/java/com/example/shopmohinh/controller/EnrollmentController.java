package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.EnrolledCourseResponse;
import com.example.shopmohinh.service.impl.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/{userId}/courses")
    public ResponseEntity<ApiResponse<List<EnrolledCourseResponse>>> getEnrolledCourses(@PathVariable Long userId) {
        List<EnrolledCourseResponse> list = enrollmentService.getEnrolledCourses(userId);
        ApiResponse<List<EnrolledCourseResponse>> response = ApiResponse.<List<EnrolledCourseResponse>>builder()
                .code(1000)
                .message("OK")
                .result(list)
                .build();
        return ResponseEntity.ok(response);
    }
}

