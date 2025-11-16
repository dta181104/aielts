package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.PermissionRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.PermissionResponse;
import com.example.shopmohinh.service.impl.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@Slf4j
public class PermissionController {
    @Autowired
    PermissionService permissionService;

    @PostMapping
    public ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request){
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<PermissionResponse>> getAll(){
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getPermission())
                .build();
    }

    @DeleteMapping("/{code}")
    public ApiResponse<PermissionResponse> delete(@PathVariable("code") String code){
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.delete(code))
                .build();
    }
}
