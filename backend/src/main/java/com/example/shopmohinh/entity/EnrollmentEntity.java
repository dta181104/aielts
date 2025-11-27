package com.example.shopmohinh.entity;

import com.example.shopmohinh.entity.course.CourseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "enrollment")
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.example.shopmohinh.entity.User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(length = 50)
    private String status;

    @Column(name = "progress_percent")
    private Integer progressPercent;

    @Column(name = "enrolled_date")
    private LocalDateTime enrolledDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;
}

