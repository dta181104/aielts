package com.example.shopmohinh.repository;

import com.example.shopmohinh.dto.response.ImageResponse;
import com.example.shopmohinh.entity.ImageEntity;
import com.example.shopmohinh.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findByProduct(Product product);
}
