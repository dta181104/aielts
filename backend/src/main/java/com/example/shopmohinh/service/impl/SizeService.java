package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.SizeRequest;
import com.example.shopmohinh.dto.response.SizeResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.SizeEntity;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.SizeMapper;
import com.example.shopmohinh.mapper.UserMapper;
import com.example.shopmohinh.repository.SizeRepository;
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
public class SizeService{
    SizeRepository sizeRepository;

    SizeMapper sizeMapper;

    UserRepository userRepository;

    UserMapper userMapper;

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public List<SizeResponse> getAll(){
        return sizeRepository.findAll().stream()
                .map(sizeMapper::toSizeResponse).toList();
    }

    public SizeResponse createSize(SizeRequest request){
        SizeEntity sizeEntity = sizeMapper.toSize(request);

        if(sizeRepository.getTop1()==null){
            sizeEntity.setCode("SZ1");
        }else{
            String code = sizeRepository.getTop1().getCode();
            sizeEntity.setCode(code.substring(0,2)+((Integer.parseInt(code.substring(2)))+1));
        }

        LocalDateTime now = LocalDateTime.now();
        sizeEntity.setCreatedDate(now);
        sizeEntity.setCreatedBy(this.getMyInfo().getCode());
        return sizeMapper.toSizeResponse(sizeRepository.save(sizeEntity));
    }

    public SizeResponse deleteSize(String code){
        SizeEntity sizeEntity = sizeRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_EXISTED));

        if(sizeEntity != null){
            sizeEntity.setDeleted(true);
        }

        return sizeMapper.toSizeResponse(sizeEntity);
    }

    public SizeResponse update(String code, SizeRequest request) {
        SizeEntity sizeEntity = sizeRepository.findByCode(code).
                orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_EXISTED));

        LocalDateTime now = LocalDateTime.now();

        sizeEntity.setUpdatedDate(now);

        Long id = getMyInfo().getId();

        sizeEntity.setUpdatedBy(String.valueOf(id));

        sizeMapper.updateSize(sizeEntity, request);

        return sizeMapper.toSizeResponse(sizeRepository.save(sizeEntity));
    }

}
