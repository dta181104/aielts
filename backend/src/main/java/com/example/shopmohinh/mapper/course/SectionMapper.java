package com.example.shopmohinh.mapper.course;

import com.example.shopmohinh.dto.request.course.SectionRequest;
import com.example.shopmohinh.dto.response.course.SectionResponse;
import com.example.shopmohinh.entity.course.SectionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    SectionEntity toEntity(SectionRequest request);

    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    SectionResponse toResponse(SectionEntity entity);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    void updateEntity(@MappingTarget SectionEntity entity, SectionRequest request);
}

