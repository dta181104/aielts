package com.example.shopmohinh.repository.course;

import com.example.shopmohinh.entity.course.SectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSectionRepository extends JpaRepository<SectionEntity, Long> {

    @Query("SELECT s FROM SectionEntity s WHERE s.course.id = :courseId AND s.deleted = false ORDER BY s.orderIndex")
    List<SectionEntity> findByCourseIdNotDeleted(@Param("courseId") Long courseId);

    @Query("SELECT s FROM SectionEntity s WHERE s.id = :id AND s.deleted = false")
    Optional<SectionEntity> findByIdNotDeleted(@Param("id") Long id);

    @Query("SELECT s FROM SectionEntity s WHERE s.category.id = :categoryId AND s.deleted = false")
    List<SectionEntity> findByCategoryIdNotDeleted(@Param("categoryId") Long categoryId);
}

