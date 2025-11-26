package com.example.shopmohinh.mapper.course;

import com.example.shopmohinh.dto.request.course.LessonRequest;
import com.example.shopmohinh.dto.response.course.LessonResponse;
import com.example.shopmohinh.entity.course.LessonEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "section", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    LessonEntity toEntity(LessonRequest request);

    @Mapping(source = "section.id", target = "sectionId")
    LessonResponse toResponse(LessonEntity entity);

    @Mapping(target = "section", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    void updateEntity(@MappingTarget LessonEntity entity, LessonRequest request);
}

