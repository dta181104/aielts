package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.SubmissionAnswerRequest;
import com.example.shopmohinh.dto.response.QuizSubmissionResponse;
import com.example.shopmohinh.dto.response.SubmissionAnswerResponse;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.entity.course.QuestionEntity;
import com.example.shopmohinh.entity.course.QuizEntity;
import com.example.shopmohinh.entity.course.QuizSubmissionEntity;
import com.example.shopmohinh.entity.course.SubmissionAnswerEntity;
import com.example.shopmohinh.repository.QuestionRepository;
import com.example.shopmohinh.repository.QuizRepository;
import com.example.shopmohinh.repository.QuizSubmissionRepository;
import com.example.shopmohinh.repository.SubmissionAnswerRepository;
import com.example.shopmohinh.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class QuizSubmissionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizSubmissionRepository submissionRepository;
    private final SubmissionAnswerRepository answerRepository;
    private final UserRepository userRepository;

    public QuizSubmissionService(QuizRepository quizRepository, QuestionRepository questionRepository, QuizSubmissionRepository submissionRepository, SubmissionAnswerRepository answerRepository, UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public QuizSubmissionResponse startSubmission(Long quizId, Long userId) {
        QuizEntity quiz = quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("Quiz not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        QuizSubmissionEntity submission = QuizSubmissionEntity.builder()
                .quiz(quiz)
                .user(user)
                .startTime(LocalDateTime.now())
                .status("DOING")
                .score(BigDecimal.ZERO)
                .build();
        QuizSubmissionEntity saved = submissionRepository.save(submission);
        return toResponse(saved);
    }

    @Transactional
    public SubmissionAnswerResponse addOrUpdateAnswer(Long submissionId, SubmissionAnswerRequest req) {
        QuizSubmissionEntity submission = submissionRepository.findById(submissionId).orElseThrow(() -> new RuntimeException("Submission not found"));
        QuestionEntity question = questionRepository.findById(req.getQuestionId()).orElseThrow(() -> new RuntimeException("Question not found"));

        Optional<SubmissionAnswerEntity> existing = answerRepository.findBySubmissionId(submissionId)
                .stream()
                .filter(a -> a.getQuestion().getId().equals(req.getQuestionId()))
                .findFirst();

        SubmissionAnswerEntity entity = existing.orElseGet(() -> SubmissionAnswerEntity.builder()
                .submission(submission)
                .question(question)
                .build());

        entity.setSelectedOption(req.getSelectedOption());
        entity.setTextAnswer(req.getTextAnswer());
        entity.setAudioUrl(req.getAudioUrl());

        // Auto-evaluate for MCQ
        if (entity.getSelectedOption() != null && question.getCorrectOption() != null) {
            entity.setIsCorrect(entity.getSelectedOption().equalsIgnoreCase(question.getCorrectOption()));
        } else {
            entity.setIsCorrect(null);
        }

        SubmissionAnswerEntity saved = answerRepository.save(entity);
        return toAnswerResponse(saved);
    }

    @Transactional
    public QuizSubmissionResponse submit(Long submissionId) {
        QuizSubmissionEntity submission = submissionRepository.findById(submissionId).orElseThrow(() -> new RuntimeException("Submission not found"));
        List<SubmissionAnswerEntity> answers = answerRepository.findBySubmissionId(submissionId);

        // Auto grade MCQ: consider only answers where question.correctOption != null
        long totalMcq = answers.stream().filter(a -> a.getQuestion().getCorrectOption() != null).count();
        long correct = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();

        BigDecimal score = BigDecimal.ZERO;
        if (totalMcq > 0) {
            score = BigDecimal.valueOf(correct)
                    .divide(BigDecimal.valueOf(totalMcq), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        submission.setScore(score);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setStatus("SUBMITTED");
        QuizSubmissionEntity saved = submissionRepository.save(submission);

        List<SubmissionAnswerResponse> ansResp = answers.stream().map(this::toAnswerResponse).collect(Collectors.toList());
        QuizSubmissionResponse resp = toResponse(saved);
        resp.setAnswers(ansResp);
        return resp;
    }

    public QuizSubmissionResponse getSubmission(Long id) {
        QuizSubmissionEntity submission = submissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Submission not found"));
        List<SubmissionAnswerEntity> answers = answerRepository.findBySubmissionId(id);
        QuizSubmissionResponse resp = toResponse(submission);
        resp.setAnswers(answers.stream().map(this::toAnswerResponse).collect(Collectors.toList()));
        return resp;
    }

    private QuizSubmissionResponse toResponse(QuizSubmissionEntity s) {
        return QuizSubmissionResponse.builder()
                .id(s.getId())
                .quizId(s.getQuiz() != null ? s.getQuiz().getId() : null)
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .startTime(s.getStartTime())
                .submitTime(s.getSubmitTime())
                .score(s.getScore())
                .status(s.getStatus())
                .teacherFeedback(s.getTeacherFeedback())
                .build();
    }

    private SubmissionAnswerResponse toAnswerResponse(SubmissionAnswerEntity a) {
        return SubmissionAnswerResponse.builder()
                .id(a.getId())
                .questionId(a.getQuestion() != null ? a.getQuestion().getId() : null)
                .selectedOption(a.getSelectedOption())
                .isCorrect(a.getIsCorrect())
                .textAnswer(a.getTextAnswer())
                .audioUrl(a.getAudioUrl())
                .gradeScore(a.getGradeScore())
                .teacherNote(a.getTeacherNote())
                .build();
    }
}
