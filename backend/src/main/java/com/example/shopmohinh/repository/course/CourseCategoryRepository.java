package com.example.shopmohinh.repository.course;

import com.example.shopmohinh.entity.course.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseCategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT c FROM CategoryEntity c ORDER BY c.id DESC")
    CategoryEntity findTopByOrderByIdDesc();
}

