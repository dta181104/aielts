package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.MaterialRequest;
import com.example.shopmohinh.dto.response.MaterialResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.MaterialEntity;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.MaterialMapper;
import com.example.shopmohinh.mapper.UserMapper;
import com.example.shopmohinh.repository.MaterialRepository;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MaterialService {
    MaterialRepository materialRepository;

    MaterialMapper materialMapper;

    UserRepository userRepository;

    UserMapper userMapper;

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public List<MaterialResponse> getAll(){
        return materialRepository.findAll().stream()
                .map(materialMapper::toResponse).toList();
    }

    public MaterialResponse create(MaterialRequest request){
        MaterialEntity materialEntity = materialMapper.toEntity(request);

        if(materialRepository.getTop1() == null){
            materialEntity.setCode("MT1");
        }else{
            String code = materialRepository.getTop1().getCode();
            materialEntity.setCode(code.substring(0,2)+((Integer.parseInt(code.substring(2)))+1));
        }

        LocalDateTime now = LocalDateTime.now();
        materialEntity.setCreatedDate(now);
        materialEntity.setCreatedBy(this.getMyInfo().getCode());
        return materialMapper.toResponse(materialRepository.save(materialEntity));
    }

    public MaterialResponse delete(String code){
        MaterialEntity materialEntity = materialRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_EXISTED));

        if(materialEntity != null){
            materialEntity.setDeleted(false);
        }

        return materialMapper.toResponse(materialEntity);
    }

    public MaterialResponse update(String code, MaterialRequest request) {
        MaterialEntity materialEntity = materialRepository.findByCode(code).
                orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_EXISTED));

        LocalDateTime now = LocalDateTime.now();

        materialEntity.setUpdatedDate(now);

        Long id = getMyInfo().getId();

        materialEntity.setUpdatedBy(String.valueOf(id));

        materialMapper.update(materialEntity, request);

        return materialMapper.toResponse(materialRepository.save(materialEntity));
    }
}
