package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.projection.ProductProjection;
import com.example.shopmohinh.dto.request.ImageRequest;
import com.example.shopmohinh.dto.request.ProductRequest;
import com.example.shopmohinh.dto.response.*;
import com.example.shopmohinh.dto.search.ProductSearch;
import com.example.shopmohinh.entity.*;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.ProductMapper;
import com.example.shopmohinh.repository.ImageRepository;
import com.example.shopmohinh.repository.ProductRepository;
import com.example.shopmohinh.util.FileUploadUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
//Thay thế cho @Autowired
//@RequiredArgsConstructor sẽ tự động tạo contructor của những method đc khai báo là final
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductService {
    ProductRepository productRepository;

    ProductMapper productMapper;

    FileUploadUtil fileUploadUtil;

    UserService userService;

    ImageRepository imageRepository;


    @Transactional
    public ProductResponse create(ProductRequest request) {

        Product product = productMapper.toProduct(request);

        if (productRepository.getTop1() == null) {
            product.setCode("SP1");
        } else {
            String code = productRepository.getTop1().getCode();
            product.setCode(code.substring(0, 2) + ((Integer.parseInt(code.substring(2))) + 1));
        }

        LocalDateTime now = LocalDateTime.now();

        product.setCreatedDate(now);

        product.setCreatedBy(userService.getMyInfo().getUsername());

        Product saveProduct = productRepository.save(product);

        this.setImages(saveProduct, request.getImages());

        return productMapper.toProductResponse(saveProduct);
    }

    private void setImages(Product product, List<ImageRequest> requests) {
        if (requests == null || requests.isEmpty()) return;

        List<ImageEntity> images = new ArrayList<>();

        for (ImageRequest request : requests) {
            String url = fileUploadUtil.uploadFile(request.getImageFile());

            ImageEntity img = new ImageEntity();
            img.setImageUrl(url);
            img.setMainImage(request.getMainImage());
            img.setProduct(product);

            images.add(img);
        }

        imageRepository.saveAll(images);
    }

    public Page<ProductResponse> getProduct(@NonNull ProductSearch request) {

        Pageable pageable = PageRequest.of(request.getPageIndex() - 1, request.getPageSize());

        Page<ProductProjection> products = productRepository.getAll(request, pageable);

        return products.map(ProductResponse::new);
    }

    public ProductResponse getDetailProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<ImageEntity> images = imageRepository.findByProduct(product);
        ProductResponse response = productMapper.toProductResponse(product);

        if(!ObjectUtils.isEmpty(images)){
            List<ImageResponse> imageResponses = images.stream()
                    .map(img -> new ImageResponse(img.getId(), img.getImageUrl(), img.isMainImage()))
                    .collect(Collectors.toList());
            response.setImages(imageResponses);
        }

        return response;
    }

    public ProductResponse delete(String code) {
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (product != null) {
            product.setDeleted(false);
        }

        return productMapper.toProductResponse(product);

    }

    public ProductResponse update(String code, ProductRequest request) {
        Product product = productRepository.findByCode(code).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        productMapper.updateProduct(product, request);

        LocalDateTime now = LocalDateTime.now();

        product.setUpdatedDate(now);

        String userName = userService.getMyInfo().getUsername();

        product.setUpdatedBy(userName);

        Product updateProduct = productRepository.save(product);

        this.setImages(updateProduct, request.getImages());

        return productMapper.toProductResponse(updateProduct);
    }
}
