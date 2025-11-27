package com.example.shopmohinh.repository;

import com.example.shopmohinh.entity.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    interface EnrolledCourseProjection {
        Long getCourseId();
        String getCourseTitle();
        String getCourseThumbnail();
        Integer getProgressPercent();
        LocalDateTime getEnrolledDate();
        String getStatus();
    }

    @Query("SELECT e.course.id as courseId, e.course.title as courseTitle, e.course.thumbnail as courseThumbnail, e.progressPercent as progressPercent, e.enrolledDate as enrolledDate, e.status as status " +
           "FROM EnrollmentEntity e WHERE e.user.id = :userId")
    List<EnrolledCourseProjection> findEnrolledCoursesByUserId(@Param("userId") Long userId);
}

