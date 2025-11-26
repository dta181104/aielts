package com.example.shopmohinh.service.course;

import com.example.shopmohinh.dto.request.course.SectionRequest;
import com.example.shopmohinh.dto.response.course.SectionResponse;
import com.example.shopmohinh.entity.course.CategoryEntity;
import com.example.shopmohinh.entity.course.CourseEntity;
import com.example.shopmohinh.entity.course.SectionEntity;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.course.SectionMapper;
import com.example.shopmohinh.repository.course.CourseCategoryRepository;
import com.example.shopmohinh.repository.course.CourseRepository;
import com.example.shopmohinh.repository.course.CourseSectionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SectionService {

    CourseSectionRepository courseSectionRepository;
    CourseRepository courseRepository;
    CourseCategoryRepository categoryRepository;
    SectionMapper sectionMapper;

    @Transactional
    public SectionResponse create(SectionRequest request) {
        CourseEntity course = courseRepository.findByIdNotDeleted(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        SectionEntity section = sectionMapper.toEntity(request);
        section.setCourse(course);
        section.setDeleted(false);

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            section.setCategory(category);
        }

        if (section.getOrderIndex() == null) {
            section.setOrderIndex(0);
        }

        SectionEntity savedSection = courseSectionRepository.save(section);
        log.info("Created section with id: {} for course: {}", savedSection.getId(), course.getId());

        return sectionMapper.toResponse(savedSection);
    }

    public List<SectionResponse> getByCourseId(Long courseId) {
        return courseSectionRepository.findByCourseIdNotDeleted(courseId).stream()
                .map(sectionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public SectionResponse getById(Long id) {
        SectionEntity section = courseSectionRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return sectionMapper.toResponse(section);
    }

    public List<SectionResponse> getByCategoryId(Long categoryId) {
        return courseSectionRepository.findByCategoryIdNotDeleted(categoryId).stream()
                .map(sectionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SectionResponse update(Long id, SectionRequest request) {
        SectionEntity section = courseSectionRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        sectionMapper.updateEntity(section, request);

        if (request.getCourseId() != null && !request.getCourseId().equals(section.getCourse().getId())) {
            CourseEntity newCourse = courseRepository.findByIdNotDeleted(request.getCourseId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            section.setCourse(newCourse);
        }

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            section.setCategory(category);
        }

        SectionEntity updatedSection = courseSectionRepository.save(section);
        log.info("Updated section with id: {}", id);

        return sectionMapper.toResponse(updatedSection);
    }

    @Transactional
    public void delete(Long id) {
        SectionEntity section = courseSectionRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        section.setDeleted(true);
        courseSectionRepository.save(section);
        log.info("Soft deleted section with id: {}", id);
    }
}

