package com.example.shopmohinh.entity.course;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "course")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "level_name", length = 100)
    private String levelName;

    @Column(name = "target_band")
    private Float targetBand;

    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(length = 500)
    private String thumbnail;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", length = 20)
    private CourseType courseType = CourseType.FULL;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectionEntity> sections = new ArrayList<>();
}

