package com.example.shopmohinh.entity.course;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "question")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizEntity quiz;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    // For MCQ, store options as JSON string; null for open-ended
    @Column(columnDefinition = "TEXT")
    private String options;

    @Column(name = "correct_option", length = 10)
    private String correctOption;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(length = 50)
    private String skill; // LISTENING / SPEAKING / WRITING / READING
}

