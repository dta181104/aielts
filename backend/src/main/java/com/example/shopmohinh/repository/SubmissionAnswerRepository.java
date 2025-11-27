package com.example.shopmohinh.repository;

import com.example.shopmohinh.entity.course.SubmissionAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionAnswerRepository extends JpaRepository<SubmissionAnswerEntity, Long> {
    List<SubmissionAnswerEntity> findBySubmissionId(Long submissionId);
}

