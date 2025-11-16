package com.example.shopmohinh.mapper;

import com.example.shopmohinh.dto.request.MaterialRequest;
import com.example.shopmohinh.dto.response.MaterialResponse;
import com.example.shopmohinh.entity.MaterialEntity;
import com.example.shopmohinh.entity.SizeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

//báo cho Maptruct bt class này để sd trong spring
@Mapper(componentModel = "spring")
public interface MaterialMapper {
    MaterialEntity toEntity(MaterialRequest request);

    void update(@MappingTarget MaterialEntity entity, MaterialRequest request);

    MaterialResponse toResponse(MaterialEntity entity);
}
