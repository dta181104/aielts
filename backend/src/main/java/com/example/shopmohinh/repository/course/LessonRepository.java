package com.example.shopmohinh.repository.course;

import com.example.shopmohinh.entity.course.LessonEntity;
import com.example.shopmohinh.entity.course.LessonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, Long> {

    @Query("SELECT l FROM LessonEntity l WHERE l.section.id = :sectionId AND l.deleted = false ORDER BY l.orderIndex")
    List<LessonEntity> findBySectionIdNotDeleted(@Param("sectionId") Long sectionId);

    @Query("SELECT l FROM LessonEntity l WHERE l.id = :id AND l.deleted = false")
    Optional<LessonEntity> findByIdNotDeleted(@Param("id") Long id);

    @Query("SELECT l FROM LessonEntity l WHERE l.section.id = :sectionId AND l.type = :type AND l.deleted = false")
    List<LessonEntity> findBySectionIdAndType(@Param("sectionId") Long sectionId, @Param("type") LessonType type);
}

