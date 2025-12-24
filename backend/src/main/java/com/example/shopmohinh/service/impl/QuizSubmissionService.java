package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.SubmissionAnswerRequest;
import com.example.shopmohinh.dto.response.QuizSubmissionResponse;
import com.example.shopmohinh.dto.response.SubmissionAnswerResponse;
import com.example.shopmohinh.dto.response.course.SpeakingGradingResponse;
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
import com.example.shopmohinh.util.FileUploadUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.IOException;
import java.util.Locale;

import com.example.shopmohinh.dto.response.course.WritingGradingResponse;
import org.springframework.web.multipart.MultipartFile;

@Service
public class QuizSubmissionService {

    FileUploadUtil fileUploadUtil;
    private final GeminiService geminiService;

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizSubmissionRepository submissionRepository;
    private final SubmissionAnswerRepository answerRepository;
    private final UserRepository userRepository;

    public QuizSubmissionService(QuizRepository quizRepository, QuestionRepository questionRepository, QuizSubmissionRepository submissionRepository, SubmissionAnswerRepository answerRepository, UserRepository userRepository, GeminiService geminiService, FileUploadUtil fileUploadUtil) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
        this.fileUploadUtil = fileUploadUtil;
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
        String questionSkill = question.getSkill();

        Optional<SubmissionAnswerEntity> existing = answerRepository.findBySubmissionId(submissionId)
                .stream()
                .filter(a -> a.getQuestion().getId().equals(req.getQuestionId()))
                .findFirst();

        SubmissionAnswerEntity entity = existing.orElseGet(() -> SubmissionAnswerEntity.builder()
                .submission(submission)
                .question(question)
                .build());

        System.out.println("DEBUG CHECK - Option: " + req.getSelectedOption());
        System.out.println("DEBUG CHECK - Text: " + req.getTextAnswer());
        System.out.println("DEBUG CHECK - Audio: " + req.getAudioFile());

        entity.setSelectedOption(req.getSelectedOption());
        entity.setTextAnswer(req.getTextAnswer());

        // Auto-evaluate for MCQ
        if ( (questionSkill.equals("LISTENING") || questionSkill.equals("READING")) &&
                entity.getSelectedOption() != null && question.getCorrectOption() != null) {
            entity.setIsCorrect(entity.getSelectedOption().equalsIgnoreCase(question.getCorrectOption()));
        } else {
            entity.setIsCorrect(null);
        }

        if (questionSkill.equals("WRITING")) {

            // Prepare inputs for Gemini grading
            Integer section = question.getSection();
            String writingTopic = question.getContent();
            String writingAnswer = entity.getTextAnswer();

            try {
                WritingGradingResponse result = geminiService.gradeWriting(section, writingTopic, writingAnswer == null ? "" : writingAnswer);
                if (result != null) {
                    // store overall band into gradeScore (if available)
                    if (result.getOverallBand() != null) {
                        // use 1 decimal place for overall band (e.g., 7.0) per IELTS convention
                        entity.setGradeScore(BigDecimal.valueOf(result.getOverallBand()).setScale(1, RoundingMode.HALF_UP));
                    }

                    // build markdown teacher note from structured feedback
                    if (result.getFeedback() != null) {
                        WritingGradingResponse.Feedback fb = result.getFeedback();
                        StringBuilder md = new StringBuilder();

                        if (fb.getGeneralFeedback() != null && !fb.getGeneralFeedback().isBlank()) {
                            md.append("- **Nhận xét chung**: ").append(fb.getGeneralFeedback()).append("\n\n");
                        }

                        if (fb.getStrongPoints() != null && !fb.getStrongPoints().isBlank()) {
                            md.append("- **Điểm mạnh**: ").append(fb.getStrongPoints()).append("\n\n");
                        }

                        if (fb.getWeakPoints() != null && !fb.getWeakPoints().isBlank()) {
                            md.append("- **Điểm yếu**: ").append(fb.getWeakPoints()).append("\n\n");
                        }

                        // Scores per criteria (force dot decimal using Locale.US)
                        Double ta = result.getTaScore();
                        Double tr = result.getTrScore();
                        if (ta != null) {
                            md.append("- **Task Achievement**: ").append(String.format(Locale.US, "%.1f", ta)).append("\n\n");
                        } else if (tr != null) {
                            md.append("- **Task Response**: ").append(String.format(Locale.US, "%.1f", tr)).append("\n\n");
                        }

                        if (result.getCcScore() != null) {
                            md.append("- **Coherence and Cohesion**: ").append(String.format(Locale.US, "%.1f", result.getCcScore())).append("\n\n");
                        }
                        if (result.getLrScore() != null) {
                            md.append("- **Lexical Resource**: ").append(String.format(Locale.US, "%.1f", result.getLrScore())).append("\n\n");
                        }
                        if (result.getGraScore() != null) {
                            md.append("- **Grammatical Range and Accuracy**: ").append(String.format(Locale.US, "%.1f", result.getGraScore())).append("\n\n");
                        }

                        // Detailed per-criterion feedback (if provided)
                        if (fb.getTaFeedback() != null && !fb.getTaFeedback().isBlank()) {
                            md.append("- **Task Achievement feedback**: ").append(fb.getTaFeedback()).append("\n\n");
                        }
                        if (fb.getTrFeedback() != null && !fb.getTrFeedback().isBlank()) {
                            md.append("- **Task Response feedback**: ").append(fb.getTrFeedback()).append("\n\n");
                        }
                        if (fb.getCcFeedback() != null && !fb.getCcFeedback().isBlank()) {
                            md.append("- **Coherence and Cohesion feedback**: ").append(fb.getCcFeedback()).append("\n\n");
                        }
                        if (fb.getLrFeedback() != null && !fb.getLrFeedback().isBlank()) {
                            md.append("- **Lexical Resource feedback**: ").append(fb.getLrFeedback()).append("\n\n");
                        }
                        if (fb.getGraFeedback() != null && !fb.getGraFeedback().isBlank()) {
                            md.append("- **Grammatical Range and Accuracy feedback**: ").append(fb.getGraFeedback()).append("\n\n");
                        }

                        entity.setTeacherNote(md.toString().trim());
                    }
                }
            } catch (IOException e) {
                // On error, save an error note so teacher can see
                entity.setTeacherNote("Lỗi khi chấm tự động: " + e.getMessage());
            }

        }

        if (questionSkill.equals("SPEAKING") && req.getAudioFile() != null) {
            String folderName = "AIELTS/submission/question_" + question.getId();
            String customFileName = "question_" + question.getId() + "_submission_" + submissionId;
            String audioUrl = fileUploadUtil.uploadAudio(req.getAudioFile(), folderName, customFileName);
            entity.setAudioUrl(audioUrl);

            Integer section = question.getSection();
            String speakingTopic = question.getContent();
            MultipartFile audioFile = req.getAudioFile();

            try {
                SpeakingGradingResponse result = geminiService.gradeSpeaking(section, speakingTopic, audioFile);
                if (result != null) {
                    // store overall band into gradeScore (if available)
                    if (result.getOverallBand() != null) {
                        entity.setGradeScore(BigDecimal.valueOf(result.getOverallBand()).setScale(1, RoundingMode.HALF_UP));
                    }

                    // build markdown teacher note from structured feedback
                    if (result.getFeedback() != null) {
                        SpeakingGradingResponse.Feedback fb = result.getFeedback();
                        StringBuilder md = new StringBuilder();

                        if (fb.getYourSpeech() != null && !fb.getYourSpeech().isBlank()) {
                            md.append("- **Your Speech**: ").append(fb.getYourSpeech()).append("\n\n");
                        }

                        if (fb.getGeneralFeedback() != null && !fb.getGeneralFeedback().isBlank()) {
                            md.append("- **Nhận xét chung**: ").append(fb.getGeneralFeedback()).append("\n\n");
                        }

                        if (fb.getStrongPoints() != null && !fb.getStrongPoints().isBlank()) {
                            md.append("- **Điểm mạnh**: ").append(fb.getStrongPoints()).append("\n\n");
                        }

                        if (fb.getWeakPoints() != null && !fb.getWeakPoints().isBlank()) {
                            md.append("- **Điểm yếu**: ").append(fb.getWeakPoints()).append("\n\n");
                        }

                        // Scores per criteria (force dot decimal using Locale.US)
                        if (result.getFcScore() != null) {
                            md.append("- **Fluency and Coherence**: ").append(String.format(Locale.US, "%.1f", result.getFcScore())).append("\n\n");
                        }
                        if (result.getLrScore() != null) {
                            md.append("- **Lexical Resource**: ").append(String.format(Locale.US, "%.1f", result.getLrScore())).append("\n\n");
                        }
                        if (result.getGraScore() != null) {
                            md.append("- **Grammatical Range and Accuracy**: ").append(String.format(Locale.US, "%.1f", result.getGraScore())).append("\n\n");
                        }
                        if (result.getPrScore() != null) {
                            md.append("- **Pronunciation**: ").append(String.format(Locale.US, "%.1f", result.getPrScore())).append("\n\n");
                        }

                        // Detailed per-criterion feedback (if provided)
                        if (fb.getFcFeedback() != null && !fb.getFcFeedback().isBlank()) {
                            md.append("- **Fluency and Coherence feedback**: ").append(fb.getFcFeedback()).append("\n\n");
                        }
                        if (fb.getLrFeedback() != null && !fb.getLrFeedback().isBlank()) {
                            md.append("- **Lexical Resource feedback**: ").append(fb.getLrFeedback()).append("\n\n");
                        }
                        if (fb.getGraFeedback() != null && !fb.getGraFeedback().isBlank()) {
                            md.append("- **Grammatical Range and Accuracy feedback**: ").append(fb.getGraFeedback()).append("\n\n");
                        }
                        if (fb.getPrFeedback() != null && !fb.getPrFeedback().isBlank()) {
                            md.append("- **Pronunciation feedback**: ").append(fb.getPrFeedback()).append("\n\n");
                        }
                        entity.setTeacherNote(md.toString().trim());
                    }
                }
            } catch (IOException e) {
                // On error, save an error note so teacher can see
                entity.setTeacherNote("Lỗi khi chấm tự động: " + e.getMessage());
            }
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

    public List<QuizSubmissionResponse> getSubmissionsByUser(Long userId) {
        List<QuizSubmissionEntity> submissions = submissionRepository.findByUserId(userId);
        return submissions.stream().map(s -> {
            QuizSubmissionResponse resp = toResponse(s);
            List<SubmissionAnswerEntity> answers = answerRepository.findBySubmissionId(s.getId());
            resp.setAnswers(answers.stream().map(this::toAnswerResponse).collect(Collectors.toList()));
            return resp;
        }).collect(Collectors.toList());
    }

    public List<QuizSubmissionResponse> getSubmissionsByQuizAndUser(Long quizId, Long userId) {
        List<QuizSubmissionEntity> submissions = submissionRepository.findAllByQuizIdAndUserId(quizId, userId);
        return submissions.stream().map(s -> {
            QuizSubmissionResponse resp = toResponse(s);
            List<SubmissionAnswerEntity> answers = answerRepository.findBySubmissionId(s.getId());
            resp.setAnswers(answers.stream().map(this::toAnswerResponse).collect(Collectors.toList()));
            return resp;
        }).collect(Collectors.toList());
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

