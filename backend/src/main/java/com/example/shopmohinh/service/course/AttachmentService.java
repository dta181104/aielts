package com.example.shopmohinh.service.course;

import com.example.shopmohinh.dto.request.course.AttachmentRequest;
import com.example.shopmohinh.dto.response.course.AttachmentResponse;
import com.example.shopmohinh.entity.course.AttachmentEntity;
import com.example.shopmohinh.entity.course.LessonEntity;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.course.AttachmentMapper;
import com.example.shopmohinh.repository.course.AttachmentRepository;
import com.example.shopmohinh.repository.course.LessonRepository;
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
public class AttachmentService {

    AttachmentRepository attachmentRepository;
    LessonRepository lessonRepository;
    AttachmentMapper attachmentMapper;

    @Transactional
    public AttachmentResponse create(AttachmentRequest request) {
        LessonEntity lesson = lessonRepository.findByIdNotDeleted(request.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        AttachmentEntity attachment = attachmentMapper.toEntity(request);
        attachment.setLesson(lesson);

        AttachmentEntity savedAttachment = attachmentRepository.save(attachment);
        log.info("Created attachment with id: {} for lesson: {}", savedAttachment.getId(), lesson.getId());

        return attachmentMapper.toResponse(savedAttachment);
    }

    public List<AttachmentResponse> getByLessonId(Long lessonId) {
        return attachmentRepository.findByLessonId(lessonId).stream()
                .map(attachmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public AttachmentResponse getById(Long id) {
        AttachmentEntity attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return attachmentMapper.toResponse(attachment);
    }

    @Transactional
    public AttachmentResponse update(Long id, AttachmentRequest request) {
        AttachmentEntity attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        attachmentMapper.updateEntity(attachment, request);

        if (request.getLessonId() != null && !request.getLessonId().equals(attachment.getLesson().getId())) {
            LessonEntity newLesson = lessonRepository.findByIdNotDeleted(request.getLessonId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            attachment.setLesson(newLesson);
        }

        AttachmentEntity updatedAttachment = attachmentRepository.save(attachment);
        log.info("Updated attachment with id: {}", id);

        return attachmentMapper.toResponse(updatedAttachment);
    }

    @Transactional
    public void delete(Long id) {
        AttachmentEntity attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        attachmentRepository.delete(attachment);
        log.info("Deleted attachment with id: {}", id);
    }
}

