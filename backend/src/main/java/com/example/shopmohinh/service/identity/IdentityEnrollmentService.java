package com.example.shopmohinh.service.identity;

import com.example.shopmohinh.dto.response.EnrolledCourseResponse;
import com.example.shopmohinh.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IdentityEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public IdentityEnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<EnrolledCourseResponse> getEnrolledCourses(Long userId) {
        return enrollmentRepository.findEnrolledCoursesByUserId(userId)
                .stream()
                .map(p -> EnrolledCourseResponse.builder()
                        .id(p.getCourseId())
                        .title(p.getCourseTitle())
                        .thumbnail(p.getCourseThumbnail())
                        .progressPercent(p.getProgressPercent())
                        .enrolledDate(p.getEnrolledDate())
                        .status(p.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}

