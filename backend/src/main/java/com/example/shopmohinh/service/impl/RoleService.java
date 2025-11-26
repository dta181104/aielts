package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.RoleRequest;
import com.example.shopmohinh.dto.response.RoleResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.Role;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.RoleMapper;
import com.example.shopmohinh.mapper.UserMapper;
import com.example.shopmohinh.repository.PermissionRepository;
import com.example.shopmohinh.repository.RoleRepository;
import com.example.shopmohinh.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
//Thay thế cho @Autowired
//@RequiredArgsConstructor sẽ tự động tạo contructor của những method đc khai báo là final
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class RoleService {
    RoleRepository roleRepository;

    PermissionRepository permissionRepository;

    RoleMapper roleMapper;

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

    public RoleResponse create(RoleRequest request){
        var role = roleMapper.toRole(request);

//        var permissions = permissionRepository.findAllById(request.getPermissions());
//
//        role.setPermissions(new HashSet<>(permissions));

        var permissionIds = request.getPermissions();

        if (permissionIds != null && !permissionIds.isEmpty()) {
            // Nếu có thì mới tìm trong DB
            var permissions = permissionRepository.findAllById(permissionIds);
            role.setPermissions(new HashSet<>(permissions));
        }

        LocalDateTime now = LocalDateTime.now();

        role.setCreatedDate(now);

        Long id = getMyInfo().getId();

        role.setCreatedBy(String.valueOf(id));

        role = roleRepository.save(role);

        return roleMapper.toRoleResponse(role);
    }


    public List<RoleResponse> getAll(){
        var roles = roleRepository.findAll();
        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(Long id){
        roleRepository.deleteById(id);
    }

    public RoleResponse roleUpdate(Long roleId, RoleRequest request) {
        Role role = roleRepository.findById(roleId).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        roleMapper.updateRole(role, request);

        LocalDateTime now = LocalDateTime.now();

        role.setUpdatedDate(now);

        Long id = getMyInfo().getId();

        role.setUpdatedBy(String.valueOf(id));

//        var permissions = permissionRepository.findAllById(request.getPermissions());

//        role.setPermissions(new HashSet<>(permissions));

        var requestedPermissions = request.getPermissions();
        log.info("DEBUG: permissions list = " + request.getPermissions());
        if (requestedPermissions != null) {
            log.info("requestedPermissions: {}", requestedPermissions);
            var permissions = permissionRepository.findAllById(requestedPermissions);
            log.info("permissions: {}", permissions);
            role.setPermissions(new HashSet<>(permissions));
        }

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }
}
