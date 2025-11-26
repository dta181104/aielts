package com.example.shopmohinh.entity.course;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import com.example.shopmohinh.entity.course.QuestionEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quiz")
public class QuizEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private LessonEntity lesson;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "pass_score")
    private Integer passScore; // percentage required to pass

    @Column(name = "duration")
    private Integer duration; // minutes

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionEntity> questions = new ArrayList<>();
}
