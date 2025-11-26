package com.example.shopmohinh.mapper.course;

import com.example.shopmohinh.dto.request.course.AttachmentRequest;
import com.example.shopmohinh.dto.response.course.AttachmentResponse;
import com.example.shopmohinh.entity.course.AttachmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    @Mapping(target = "lesson", ignore = true)
    AttachmentEntity toEntity(AttachmentRequest request);

    @Mapping(source = "lesson.id", target = "lessonId")
    AttachmentResponse toResponse(AttachmentEntity entity);

    @Mapping(target = "lesson", ignore = true)
    void updateEntity(@MappingTarget AttachmentEntity entity, AttachmentRequest request);
}

