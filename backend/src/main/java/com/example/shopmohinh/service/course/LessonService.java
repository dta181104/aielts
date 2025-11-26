package com.example.shopmohinh.service.course;

import com.example.shopmohinh.dto.request.course.LessonRequest;
import com.example.shopmohinh.dto.response.course.LessonResponse;
import com.example.shopmohinh.entity.course.LessonEntity;
import com.example.shopmohinh.entity.course.LessonType;
import com.example.shopmohinh.entity.course.SectionEntity;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.course.LessonMapper;
import com.example.shopmohinh.repository.course.LessonRepository;
import com.example.shopmohinh.repository.course.CourseSectionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LessonService {

    LessonRepository lessonRepository;
    CourseSectionRepository courseSectionRepository;
    LessonMapper lessonMapper;

    @Transactional
    public LessonResponse create(LessonRequest request) {
        SectionEntity section = courseSectionRepository.findByIdNotDeleted(request.getSectionId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        LessonEntity lesson = lessonMapper.toEntity(request);
        lesson.setSection(section);
        lesson.setCreatedDate(LocalDateTime.now());
        lesson.setDeleted(false);

        if (lesson.getDuration() == null) {
            lesson.setDuration(0);
        }
        if (lesson.getOrderIndex() == null) {
            lesson.setOrderIndex(0);
        }

        LessonEntity savedLesson = lessonRepository.save(lesson);
        log.info("Created lesson with id: {} for section: {}", savedLesson.getId(), section.getId());

        return lessonMapper.toResponse(savedLesson);
    }

    public List<LessonResponse> getBySectionId(Long sectionId) {
        return lessonRepository.findBySectionIdNotDeleted(sectionId).stream()
                .map(lessonMapper::toResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse getById(Long id) {
        LessonEntity lesson = lessonRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return lessonMapper.toResponse(lesson);
    }

    public List<LessonResponse> getBySectionIdAndType(Long sectionId, LessonType type) {
        return lessonRepository.findBySectionIdAndType(sectionId, type).stream()
                .map(lessonMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LessonResponse update(Long id, LessonRequest request) {
        LessonEntity lesson = lessonRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        lessonMapper.updateEntity(lesson, request);
        lesson.setUpdatedDate(LocalDateTime.now());

        if (request.getSectionId() != null && !request.getSectionId().equals(lesson.getSection().getId())) {
            SectionEntity newSection = courseSectionRepository.findByIdNotDeleted(request.getSectionId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            lesson.setSection(newSection);
        }

        LessonEntity updatedLesson = lessonRepository.save(lesson);
        log.info("Updated lesson with id: {}", id);

        return lessonMapper.toResponse(updatedLesson);
    }

    @Transactional
    public void delete(Long id) {
        LessonEntity lesson = lessonRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        lesson.setDeleted(true);
        lessonRepository.save(lesson);
        log.info("Soft deleted lesson with id: {}", id);
    }
}

