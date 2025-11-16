package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.BrandRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.BrandResponse;
import com.example.shopmohinh.service.impl.BrandService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
@Slf4j
public class BrandController {
    BrandService brandService;

    @GetMapping
    public ApiResponse<List<BrandResponse>> getAll(){
        return ApiResponse.<List<BrandResponse>>builder()
                .result(brandService.getAll())
                .build();
    }

    @DeleteMapping("/{code}")
    public ApiResponse<BrandResponse> delete(@PathVariable("code") String code){
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.delete(code))
                .build();
    }

    @PostMapping
    public ApiResponse<BrandResponse> create(@RequestBody @Valid BrandRequest request){
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.create(request))
                .build();
    }

    @PutMapping("/{code}")
    public ApiResponse<BrandResponse> update(@RequestBody @Valid BrandRequest request,
                                                @PathVariable("code") String code){
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.update(code,request))
                .build();
    }
}
