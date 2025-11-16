package com.example.shopmohinh.mapper;

import com.example.shopmohinh.dto.request.SizeRequest;
import com.example.shopmohinh.dto.request.UserCreationRequest;
import com.example.shopmohinh.dto.request.UserUpdateRequest;
import com.example.shopmohinh.dto.response.SizeResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.SizeEntity;
import com.example.shopmohinh.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SizeMapper {

    SizeEntity toSize(SizeRequest request);

    void updateSize(@MappingTarget SizeEntity size, SizeRequest request);

    SizeResponse toSizeResponse(SizeEntity size);
}
