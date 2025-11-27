package com.example.shopmohinh.controller.identity;

import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.EnrolledCourseResponse;
import com.example.shopmohinh.service.identity.IdentityEnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/identity")
public class IdentityEnrollmentController {

    private final IdentityEnrollmentService enrollmentService;

    public IdentityEnrollmentController(IdentityEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/users/{userId}/courses")
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

