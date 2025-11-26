package com.example.shopmohinh.repository.course;

import com.example.shopmohinh.entity.course.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {

    @Query("SELECT a FROM AttachmentEntity a WHERE a.lesson.id = :lessonId")
    List<AttachmentEntity> findByLessonId(@Param("lessonId") Long lessonId);
}

