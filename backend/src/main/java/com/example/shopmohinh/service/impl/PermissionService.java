package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.PermissionRequest;
import com.example.shopmohinh.dto.response.PermissionResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.Permission;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.PermissionMapper;
import com.example.shopmohinh.mapper.UserMapper;
import com.example.shopmohinh.repository.PermissionRepository;
import com.example.shopmohinh.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
//Thay thế cho @Autowired
//@RequiredArgsConstructor sẽ tự động tạo contructor của những method đc khai báo là final
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class PermissionService {

    PermissionRepository permissionRepository;

    PermissionMapper permissionMapper;

    UserRepository userRepository;

    UserMapper userMapper;

    // lấy thông tin người đang đăng nhập
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public PermissionResponse create(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);

        LocalDateTime now = LocalDateTime.now();

        permission.setCreatedDate(now);

        Long id = getMyInfo().getId();

        permission.setCreatedBy(String.valueOf(id));

        permission = permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getPermission(){

        return permissionRepository.getAll().stream()
                .map(permissionMapper::toPermissionResponse).toList();
    }

    public PermissionResponse delete(String code){
        Permission permission = permissionRepository.findByCode(code).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        permission.setDeleted(true);

        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }
}
