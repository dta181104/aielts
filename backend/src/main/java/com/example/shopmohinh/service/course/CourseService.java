package com.example.shopmohinh.service.course;

import com.example.shopmohinh.dto.request.course.CourseRequest;
import com.example.shopmohinh.dto.response.course.CourseResponse;
import com.example.shopmohinh.entity.course.CourseEntity;
import com.example.shopmohinh.entity.course.CourseStatus;
import com.example.shopmohinh.entity.course.CourseType;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.course.CourseMapper;
import com.example.shopmohinh.repository.course.CourseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseService {

    CourseRepository courseRepository;
    CourseMapper courseMapper;

    @Transactional
    public CourseResponse create(CourseRequest request) {
        CourseEntity course = courseMapper.toEntity(request);
        course.setCreatedDate(LocalDateTime.now());
        course.setDeleted(false);

        if (course.getCourseType() == null) {
            course.setCourseType(CourseType.FULL);
        }
        if (course.getStatus() == null) {
            course.setStatus(CourseStatus.DRAFT);
        }

        CourseEntity savedCourse = courseRepository.save(course);
        log.info("Created course with id: {}", savedCourse.getId());

        return courseMapper.toResponse(savedCourse);
    }

    public Page<CourseResponse> getAllCourses(Pageable pageable) {
        return courseRepository.findAllNotDeleted(pageable)
                .map(courseMapper::toResponse);
    }

    public CourseResponse getById(Long id) {
        CourseEntity course = courseRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return courseMapper.toResponse(course);
    }

    public Page<CourseResponse> getCoursesByStatus(CourseStatus status, Pageable pageable) {
        return courseRepository.findByStatus(status, pageable)
                .map(courseMapper::toResponse);
    }

    public Page<CourseResponse> getCoursesByType(CourseType type, Pageable pageable) {
        return courseRepository.findByCourseType(type, pageable)
                .map(courseMapper::toResponse);
    }

    public Page<CourseResponse> searchCourses(CourseStatus status, CourseType type, String keyword, Pageable pageable) {
        return courseRepository.searchCourses(status, type, keyword, pageable)
                .map(courseMapper::toResponse);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        CourseEntity course = courseRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        courseMapper.updateEntity(course, request);
        CourseEntity updatedCourse = courseRepository.save(course);

        log.info("Updated course with id: {}", id);
        return courseMapper.toResponse(updatedCourse);
    }

    @Transactional
    public void delete(Long id) {
        CourseEntity course = courseRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        course.setDeleted(true);
        courseRepository.save(course);
        log.info("Soft deleted course with id: {}", id);
    }
}

