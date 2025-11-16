package com.example.shopmohinh.mapper;

import com.example.shopmohinh.dto.request.PermissionRequest;
import com.example.shopmohinh.dto.request.RoleRequest;
import com.example.shopmohinh.dto.request.UserUpdateRequest;
import com.example.shopmohinh.dto.response.PermissionResponse;
import com.example.shopmohinh.dto.response.RoleResponse;
import com.example.shopmohinh.entity.Permission;
import com.example.shopmohinh.entity.Role;
import com.example.shopmohinh.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

//báo cho Maptruct bt class này để sd trong spring
@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions",ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role request);

    @Mapping(target = "permissions",ignore = true)
    void updateRole(@MappingTarget Role role, RoleRequest request);
}
