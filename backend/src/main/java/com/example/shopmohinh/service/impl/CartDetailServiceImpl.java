package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.CartDetailRequest;
import com.example.shopmohinh.dto.response.CartDetailResponse;
import com.example.shopmohinh.entity.CartDetailEntity;
import com.example.shopmohinh.entity.CartEntity;
import com.example.shopmohinh.entity.Product;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.CartDetailMapper;
import com.example.shopmohinh.mapper.ProductMapper;
import com.example.shopmohinh.repository.CartDetailRepository;
import com.example.shopmohinh.repository.CartRepository;
import com.example.shopmohinh.repository.ProductRepository;
import com.example.shopmohinh.service.CartDetailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartDetailServiceImpl implements CartDetailService {
    CartDetailRepository cartDetailRepository;

    CartRepository cartRepository;

    ProductRepository productRepository;

    CartDetailMapper cartDetailMapper;

    ProductMapper productMapper;

    @Override
    @Transactional
    public CartDetailResponse addProductToCartDetail(CartDetailRequest request) {
        CartDetailEntity entity = cartDetailRepository.findByProductId(request.getProductId());
        if(entity != null){
            throw new AppException(ErrorCode.PRODUCT_HAS_BEEN_EXITED);
        }
        this.genCodeCart(entity);
        entity.setQuantity(request.getQuantity());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CartEntity cartEntity = cartRepository.findByUsername(username);
        if (cartEntity != null) {
            entity.setCart(cartEntity);
        }else {
            throw new AppException(ErrorCode.CART_NOT_FOUND);
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        entity.setProduct(product);
        return this.toResponse(cartDetailRepository.save(entity));
    }

    @Override
    public String deleteProduct(List<Long> productIds) {
        if(ObjectUtils.isEmpty(productIds)) {
            throw new AppException(ErrorCode.PRODUCT_DO_NOT_EMPTY);
        }
        int deleteNumber = cartDetailRepository.deleteByProductIds(productIds);
        return "Số sản phẩm đã xóa khỏi giỏ hàng: " + deleteNumber;
    }

    private void genCodeCart(CartDetailEntity saveCart){
        CartDetailEntity top1 = cartDetailRepository.getTop1();
        String defaultCode = "CART_DETAIL_1";
        if(top1 == null){
            saveCart.setCode(defaultCode);
        }else{
            String code = top1.getCode();
            saveCart.setCode(code.substring(0,defaultCode.length()-1)+((Integer.parseInt(code.substring(defaultCode.length()-1)))+1));
        }
    }

    private CartDetailResponse toResponse(CartDetailEntity entity) {
        if (entity == null) return null;

        CartDetailResponse response = new CartDetailResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setQuantity(entity.getQuantity());

        if (entity.getProduct() != null) {
            response.setProductResponse(productMapper.toProductResponse(entity.getProduct()));
        }

        if (entity.getCart() != null) {
            response.setCartCode(entity.getCart().getCode());
            if (entity.getCart().getUser() != null) {
                response.setUsername(entity.getCart().getUser().getUsername());
            }
        }
        return response;
    }
}
