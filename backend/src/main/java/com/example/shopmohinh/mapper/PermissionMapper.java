package com.example.shopmohinh.mapper;

import com.example.shopmohinh.dto.request.PermissionRequest;
import com.example.shopmohinh.dto.request.UserCreationRequest;
import com.example.shopmohinh.dto.response.PermissionResponse;
import com.example.shopmohinh.entity.Permission;
import com.example.shopmohinh.entity.User;
import org.mapstruct.Mapper;

//báo cho Maptruct bt class này để sd trong spring
@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission request);
}
