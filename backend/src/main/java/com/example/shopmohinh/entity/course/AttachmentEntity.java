package com.example.shopmohinh.entity.course;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attachment")
public class AttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private LessonEntity lesson;

    @Column(length = 255)
    private String name;

    @Column(length = 500, nullable = false)
    private String url;

    @Column(name = "file_type", length = 20)
    private String fileType;
}

