package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.SizeRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.SizeResponse;
import com.example.shopmohinh.service.impl.SizeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/size")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SizeController {

    SizeService sizeService;

    @GetMapping
    public ApiResponse<List<SizeResponse>> getAll(){
        return ApiResponse.<List<SizeResponse>>builder()
                .result(sizeService.getAll())
                .build();
    }

    @DeleteMapping("/{code}")
    public ApiResponse<SizeResponse> delete(@PathVariable("code") String code){
        return ApiResponse.<SizeResponse>builder()
                .result(sizeService.deleteSize(code))
                .build();
    }

    @PostMapping
    public ApiResponse<SizeResponse> create(@RequestBody @Valid SizeRequest request){
        return ApiResponse.<SizeResponse>builder()
                .result(sizeService.createSize(request))
                .build();
    }

    @PutMapping("/{code}")
    public ApiResponse<SizeResponse> update(@RequestBody @Valid SizeRequest request,
                                            @PathVariable("code") String code){
        return ApiResponse.<SizeResponse>builder()
                .result(sizeService.update(code,request))
                .build();
    }
}
