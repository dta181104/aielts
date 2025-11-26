package com.example.shopmohinh.mapper;

import com.example.shopmohinh.dto.request.UserCreationRequest;
import com.example.shopmohinh.dto.request.UserUpdateRequest;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

//báo cho Maptruct bt class này để sd trong spring
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles",ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(target = "roles",ignore = true)
    @Mapping(target = "pass",ignore = true)
    @Mapping(target = "avatar",ignore = true)
    @Mapping(target = "deleted",ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    //      Map email với phone
//      @Mapping(source = "email",target = "phone")
//      không mapping trường phone
//      @Mapping(target = "phone",ignore = true)
    UserResponse toUserResponse(User user);
}
