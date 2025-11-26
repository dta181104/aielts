package com.example.shopmohinh.repository.course;

import com.example.shopmohinh.entity.course.CourseEntity;
import com.example.shopmohinh.entity.course.CourseStatus;
import com.example.shopmohinh.entity.course.CourseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    @Query("SELECT c FROM CourseEntity c WHERE c.deleted = false")
    Page<CourseEntity> findAllNotDeleted(Pageable pageable);

    @Query("SELECT c FROM CourseEntity c WHERE c.deleted = false AND c.id = :id")
    Optional<CourseEntity> findByIdNotDeleted(@Param("id") Long id);

    @Query("SELECT c FROM CourseEntity c WHERE c.deleted = false AND c.status = :status")
    Page<CourseEntity> findByStatus(@Param("status") CourseStatus status, Pageable pageable);

    @Query("SELECT c FROM CourseEntity c WHERE c.deleted = false AND c.courseType = :type")
    Page<CourseEntity> findByCourseType(@Param("type") CourseType type, Pageable pageable);

    @Query("SELECT c FROM CourseEntity c WHERE c.deleted = false " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:type IS NULL OR c.courseType = :type) " +
           "AND (:keyword IS NULL OR c.title LIKE %:keyword% OR c.description LIKE %:keyword%)")
    Page<CourseEntity> searchCourses(
        @Param("status") CourseStatus status,
        @Param("type") CourseType type,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}

