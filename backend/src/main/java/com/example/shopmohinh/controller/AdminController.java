package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.UserCreationRequest;
import com.example.shopmohinh.dto.request.UserUpdateRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    //thêm @Valid để create chạy validate
    @PostMapping("/create")
    public ApiResponse<UserResponse> createAcc(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createdUser(request))
                .build();
    }

    @GetMapping("/accounts")
    public ApiResponse<List<UserResponse>>getUsers() {
//      SecurityContextHolder chứa user đang đăng nhập(Request)
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("username: {}", authentication.getName());

//       authentication.getAuthorities() trả về một tập hợp các quyền của người dùng
        authentication.getAuthorities()

//      forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority())) duyệt qua từng quyền và ghi log tên của từng quyền đó
                .forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("find/{code}")
    public ApiResponse<UserResponse> findUserByCode(@PathVariable("code") String code) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.findUser(code))
                .build();
    }

    @PutMapping("update/{code}")
    public ApiResponse<UserResponse> updateAcc(@PathVariable("code") String code,
                                                 @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.userUpdate(code,request))
                .build();

    }

    @DeleteMapping("delete/{code}")
    public ApiResponse<UserResponse> deleteAcc(@PathVariable("code") String code){
        return ApiResponse.<UserResponse>builder()
                .result(userService.deleteUser(code))
                .build();
    }
}
