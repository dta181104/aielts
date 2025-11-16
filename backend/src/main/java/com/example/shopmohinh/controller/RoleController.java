package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.RoleRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.RoleResponse;
import com.example.shopmohinh.service.impl.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@Slf4j
public class RoleController {
    @Autowired
    RoleService roleService;

    @PostMapping
    public ApiResponse<RoleResponse> create(@RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getAll(){
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id){
        roleService.delete(id);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{roleID}")
    public ApiResponse<RoleResponse> updateUser(@PathVariable("roleID") Long roleId,
                                                @RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.roleUpdate(roleId,request))
                .build();

    }
}
