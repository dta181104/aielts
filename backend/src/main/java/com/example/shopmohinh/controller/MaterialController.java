package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.MaterialRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.MaterialResponse;
import com.example.shopmohinh.service.impl.MaterialService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
@Slf4j
public class MaterialController {
    MaterialService materialService;

    @GetMapping
    public ApiResponse<List<MaterialResponse>> getAll(){
        return ApiResponse.<List<MaterialResponse>>builder()
                .result(materialService.getAll())
                .build();
    }

    @DeleteMapping("/{code}")
    public ApiResponse<MaterialResponse> delete(@PathVariable("code") String code){
        return ApiResponse.<MaterialResponse>builder()
                .result(materialService.delete(code))
                .build();
    }

    @PostMapping
    public ApiResponse<MaterialResponse> create(@RequestBody @Valid MaterialRequest request){
        return ApiResponse.<MaterialResponse>builder()
                .result(materialService.create(request))
                .build();
    }

    @PutMapping("/{code}")
    public ApiResponse<MaterialResponse> update(@RequestBody @Valid MaterialRequest request,
                                            @PathVariable("code") String code){
        return ApiResponse.<MaterialResponse>builder()
                .result(materialService.update(code,request))
                .build();
    }
}
