package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.UserCreationRequest;
import com.example.shopmohinh.dto.request.UserUpdateRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    //chuyền vào token để lấy thông tin bản thân
    @GetMapping("/myInfo")
    public ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PutMapping("/{code}")
    public ApiResponse<UserResponse> updateMyAcc(@PathVariable("code") String code,
                           @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.userUpdate(code,request))
                .build();

    }

    @DeleteMapping("/{code}")
    public ApiResponse<UserResponse> deleteMyAcc(@PathVariable("code") String code){
        return ApiResponse.<UserResponse>builder()
                .result(userService.deleteUser(code))
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@ModelAttribute UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createdUser(request))
                .build();
    }
}
