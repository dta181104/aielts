package com.example.shopmohinh.entity.course;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "lesson")
public class LessonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private LessonType type;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private Integer duration = 0;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachmentEntity> attachments = new ArrayList<>();
}

