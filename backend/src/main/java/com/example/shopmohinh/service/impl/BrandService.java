package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.BrandRequest;
import com.example.shopmohinh.dto.response.BrandResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.BrandEntity;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.BrandMapper;
import com.example.shopmohinh.mapper.UserMapper;
import com.example.shopmohinh.repository.BrandRepository;
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
public class BrandService {
    BrandRepository brandRepository;

    BrandMapper brandMapper;

    UserRepository userRepository;

    UserMapper userMapper;

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public List<BrandResponse> getAll(){
        return brandRepository.findAll().stream()
                .map(brandMapper::toBrandResponse).toList();
    }

    public BrandResponse create(BrandRequest request){
        BrandEntity brandEntity = brandMapper.toBrand(request);

        if(brandRepository.getTop1()==null){
            brandEntity.setCode("BR1");
        }else{
            String code = brandRepository.getTop1().getCode();
            brandEntity.setCode(code.substring(0,2)+((Integer.parseInt(code.substring(2)))+1));
        }

        LocalDateTime now = LocalDateTime.now();
        brandEntity.setCreatedDate(now);
        brandEntity.setCreatedBy(this.getMyInfo().getCode());
        return brandMapper.toBrandResponse(brandRepository.save(brandEntity));
    }

    public BrandResponse delete(String code){
        BrandEntity brandEntity = brandRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_EXISTED));

        if(brandEntity != null){
            brandEntity.setDeleted(false);
        }

        return brandMapper.toBrandResponse(brandEntity);
    }

    public BrandResponse update(String code, BrandRequest request) {
        BrandEntity brandEntity = brandRepository.findByCode(code).
                orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_EXISTED));

        LocalDateTime now = LocalDateTime.now();

        brandEntity.setUpdatedDate(now);

        Long id = getMyInfo().getId();

        brandEntity.setUpdatedBy(String.valueOf(id));

        brandMapper.updateBrand(brandEntity, request);

        return brandMapper.toBrandResponse(brandRepository.save(brandEntity));
    }

}
