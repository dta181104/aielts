package com.example.shopmohinh.entity.course;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "submission_answer")
public class SubmissionAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private QuizSubmissionEntity submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @Column(name = "selected_option", length = 50)
    private String selectedOption;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    // Change scale to 1 to store values like 7.0 (one decimal place) instead of two decimals
    @Column(name = "grade_score", precision = 5, scale = 1)
    private BigDecimal gradeScore;

    @Column(name = "teacher_note", columnDefinition = "TEXT")
    private String teacherNote;
}
