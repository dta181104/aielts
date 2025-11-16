package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.CategoryRequest;
import com.example.shopmohinh.dto.response.CategoryResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.Category;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.CategoryMapper;
import com.example.shopmohinh.mapper.UserMapper;
import com.example.shopmohinh.repository.CategoryRepository;
import com.example.shopmohinh.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
//Thay thế cho @Autowired
//@RequiredArgsConstructor sẽ tự động tạo contructor của những method đc khai báo là final
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class CategoryService {
    CategoryRepository categoryRepository;

    CategoryMapper categoryMapper;

    UserRepository userRepository;

    UserMapper userMapper;

    // lấy thông tin người đang đăng nhập
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public List<CategoryResponse> getAll(){

        var category = categoryRepository.getAll();

        return category.stream().map(categoryMapper::toCategoryResponse).toList();
    }

    public CategoryResponse create(CategoryRequest request){

        Category category = categoryMapper.toCategory(request);

        if(categoryRepository.getTop1()==null){
            category.setCode("Cate1");
        }else{
            String code = categoryRepository.getTop1().getCode();
            category.setCode(code.substring(0,4)+((Integer.parseInt(code.substring(4)))+1));
        }

        LocalDateTime now = LocalDateTime.now();

        category.setCreatedDate(now);

        Long id = getMyInfo().getId();

        category.setCreatedBy(String.valueOf(id));

        category = categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(category);
    }

    public CategoryResponse delete(String code){
        Category category = categoryRepository.findByCode(code).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if(category != null){
            category.setDeleted(false);
        }
        return categoryMapper.toCategoryResponse(category);
    }

    public CategoryResponse update(String code, CategoryRequest request) {
        Category category = categoryRepository.findByCode(code).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        categoryMapper.updateCategory(category, request);

        LocalDateTime now = LocalDateTime.now();

        category.setUpdatedDate(now);

        Long id = getMyInfo().getId();

        category.setUpdatedBy(String.valueOf(id));

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAllPaging(Pageable pageable) {

        var category = categoryRepository.getAllPaging(pageable);

        return category.stream().map(categoryMapper::toCategoryResponse).toList();
    }
    public Double getAllTotalPage() {
        int totalPage=categoryRepository.getAllTotalPage().size();
        return Math.ceil(totalPage/3.0);
    }
    public CategoryResponse findUser(Long id) {
        log.info("In method get category by id");
        return categoryMapper.toCategoryResponse(categoryRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        ));
    }

    public List<CategoryResponse> findByAll(String name, String status, Pageable pageable) {

        var category = categoryRepository.findByAll(name,status,pageable);

        return category.stream().map(categoryMapper::toCategoryResponse).toList();
    }

    public List<CategoryResponse> findAllTotalPage(String name, String status) {
        var category = categoryRepository.findAllTotalPage(name,status);
        return category.stream().map(categoryMapper::toCategoryResponse).toList();
    }

}
