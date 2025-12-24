package com.example.shopmohinh.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.File;
import com.google.genai.types.UploadFileConfig;
import com.example.shopmohinh.dto.response.course.WritingGradingResponse;
import com.example.shopmohinh.dto.response.course.SpeakingGradingResponse;
import com.google.genai.types.Type;
import com.google.genai.types.Schema;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @SuppressWarnings("unused")
//    @Value("${gemini.api.key}")
//    private String geminiApiKey;
    @Value("${gemini.api.model}")
    private String geminiApiModel;

//    private Client client;
    private final Client client;
    private final ObjectMapper objectMapper = new ObjectMapper();

//    @PostConstruct
//    public void init() {
//        this.client = Client.builder()
//                .apiKey(geminiApiKey)
//                .build();
//    }

    public WritingGradingResponse gradeWriting(Integer section, String writingTopic, String writingAnswer) throws IOException {

        String systemInstruction = "Bạn là giám khảo IELTS Writing.";
        String promptText = buildWritingPrompt(section, writingTopic, writingAnswer);
        System.out.println(promptText);

        Content content = Content.fromParts(Part.fromText(promptText));
        String firstScoreStr = (section == 1) ? "taScore" : "trScore";
        String firstFeedbackStr = (section == 1) ? "taFeedback" : "trFeedback";

        Schema responseSchema =
                Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(ImmutableMap.of(
                                "overallBand", Schema.builder().type(Type.Known.NUMBER).build(),
                                firstScoreStr, Schema.builder().type(Type.Known.NUMBER).build(),
                                "ccScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "lrScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "graScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "feedback", Schema.builder().type(Type.Known.OBJECT)
                                        .properties(ImmutableMap.of(
                                                "generalFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "strongPoints", Schema.builder().type(Type.Known.STRING).build(),
                                                "weakPoints", Schema.builder().type(Type.Known.STRING).build(),
                                                firstFeedbackStr, Schema.builder().type(Type.Known.STRING).build(),
                                                "ccFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "lrFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "graFeedback", Schema.builder().type(Type.Known.STRING).build()
                                        ))
                                        .required(ImmutableList.of("generalFeedback", "strongPoints", "weakPoints", firstFeedbackStr, "ccFeedback", "lrFeedback", "graFeedback"))
                                        .build()
                        ))
                        .required(ImmutableList.of("overallBand", firstScoreStr, "ccScore", "lrScore", "graScore", "feedback"))
                        .build();


        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .responseSchema(responseSchema)
                        .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
                        .candidateCount(1)
                        .build();

        try {
            GenerateContentResponse response = client.models.generateContent(
                    geminiApiModel,
                    content,
                    config
            );

            // Read raw response first and check for null/blank before any string operations
            String rawResult = response.text();
            if (rawResult == null || rawResult.isBlank()) {
                throw new RuntimeException("Gemini API không trả về kết quả.");
            }
            System.out.println("Gemini API trả về kết quả: " + rawResult);

            return objectMapper.readValue(rawResult, WritingGradingResponse.class);

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API Gemini: " + e.getMessage());
            throw new IOException("Không thể chấm điểm Writing qua Gemini.", e);
        }
    }

    public SpeakingGradingResponse gradeSpeaking(Integer section, String speakingTopic, MultipartFile audioFile) throws IOException {

        // Upload file audio lên Gemini
        File uploadedAudio = uploadAudioFile(audioFile);

        String systemInstruction = "Bạn là giám khảo IELTS Speaking.";
        String promptText = buildSpeakingPrompt(section, speakingTopic);
        System.out.println(promptText);

        Content content = Content.fromParts(
                Part.fromText(promptText),
                Part.fromUri(uploadedAudio.uri().get(), uploadedAudio.mimeType().get()));

        Schema responseSchema =
                Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(ImmutableMap.of(
                                "overallBand", Schema.builder().type(Type.Known.NUMBER).build(),
                                "fcScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "lrScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "graScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "prScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "feedback", Schema.builder().type(Type.Known.OBJECT)
                                        .properties(ImmutableMap.of(
                                                "yourSpeech", Schema.builder().type(Type.Known.STRING).build(),
                                                "generalFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "strongPoints", Schema.builder().type(Type.Known.STRING).build(),
                                                "weakPoints", Schema.builder().type(Type.Known.STRING).build(),
                                                "fcFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "lrFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "graFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "prFeedback", Schema.builder().type(Type.Known.STRING).build()
                                        ))
                                        .required(ImmutableList.of("yourSpeech", "generalFeedback", "strongPoints", "weakPoints", "fcFeedback", "lrFeedback", "graFeedback", "prFeedback"))
                                        .build()
                        ))
                        .required(ImmutableList.of("overallBand", "fcScore", "lrScore", "graScore", "prScore", "feedback"))
                        .build();


        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .responseSchema(responseSchema)
                        .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
                        .candidateCount(1)
                        .build();

        try {
            GenerateContentResponse response = client.models.generateContent(
                    geminiApiModel,
                    content,
                    config
            );

            // Read raw response first and check for null/blank before any string operations
            String rawResult = response.text();
            if (rawResult == null || rawResult.isBlank()) {
                throw new RuntimeException("Gemini API không trả về kết quả.");
            }
            System.out.println("Gemini API trả về kết quả: " + rawResult);

            return objectMapper.readValue(rawResult, SpeakingGradingResponse.class);

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API Gemini: " + e.getMessage());
            throw new IOException("Không thể chấm điểm Speaking qua Gemini.", e);
        }
    }

    private File uploadAudioFile(MultipartFile audioFile) throws IOException {
        byte[] audioBytes = audioFile.getBytes();

        UploadFileConfig uploadConfig = UploadFileConfig.builder()
                .mimeType(audioFile.getContentType())
                .displayName(audioFile.getOriginalFilename())
                .build();

        File file = client.files.upload(
                audioBytes,
                uploadConfig
        );

        File uploadedFile = client.files.get(file.name().get(), null);
        System.out.println("File audio đã được tải lên Gemini: " + uploadedFile.name().get());
        return uploadedFile;
    }

    private String buildWritingPrompt(Integer section, String topic, String answerContent) {
        // 1. Xác định tiêu chí chấm dựa trên Task 1 hay Task 2
        String firstCriteria = (section == 1) ? "Task Achievement" : "Task Response";
        String firstScoreStr = (section == 1) ? "taScore" : "trScore";

        // 2. Thêm chỉ dẫn về định dạng Feedback để FE hiển thị đẹp hơn
        return String.format("""
    Hãy chấm bài một cách công tâm.
    Yêu cầu đề bài IELTS Writing Task %d:
    %s
    ---
    Nội dung bài viết của thí sinh:
    %s
    ---
    NHIỆM VỤ CỦA BẠN:
    Đánh giá bài viết dựa trên 4 tiêu chí chấm thi IELTS chuẩn:
    1. %s (%s) - Lưu ý: Đây là Task %d
    2. Coherence and Cohesion (ccScore)
    3. Lexical Resource (lrScore)
    4. Grammatical Range and Accuracy (graScore)
    
    YÊU CẦU ĐẦU RA:
    1. Điểm số: Chấm từng tiêu chí thang điểm 1.0 - 9.0 (bước nhảy 0.5).
    2. Overall Band: Tính trung bình cộng 4 tiêu chí (làm tròn theo quy tắc IELTS: .25 lên .5, .75 lên 1.0).
    3. Feedback (Quan trọng):
       - Phải viết bằng TIẾNG VIỆT, giọng văn xây dựng, khuyến khích.
       - Cấu trúc bắt buộc trong feedback:
         + Nhận xét chung: ...
         + Điểm mạnh: ...
         + Điểm yếu: (Chỉ ra lỗi sai cụ thể trong bài nếu có và cách sửa)
         + Nhận xét 4 tiêu chí
    """,
                section, topic, answerContent, firstCriteria, firstScoreStr, section);
    }

    private String buildSpeakingPrompt(Integer section, String topic) {
        return String.format("""
    Hãy chấm bài một cách công tâm. Hãy nhận diện văn bản dựa trên file audio được gửi, lọc các âm thanh không liên quan và trả lại NGUYÊN VĂN văn bản trong mục yourSpeech của phần feedback.
    Nếu không thể nhận diện được gì trong file audio, hãy trả lại trong mục yourSpeech "Tôi không thể nhận diện âm thanh trong bài nói của bạn" và chấm điểm 1.0 cho tất cả tiêu chí.
    Yêu cầu đề bài IELTS Speaking Task %d:
    %s
    ---
    NHIỆM VỤ CỦA BẠN:
    Đánh giá bài nói dựa trên 4 tiêu chí chấm thi IELTS chuẩn:
    1. Fluency and Coherence (fcScore)
    2. Lexical Resource (lrScore)
    3. Grammatical Range and Accuracy (graScore)
    4. Pronunciation (prScore)
    
    YÊU CẦU ĐẦU RA:
    1. Điểm số: Chấm từng tiêu chí thang điểm 1.0 - 9.0 (bước nhảy 0.5).
    2. Overall Band: Tính trung bình cộng 4 tiêu chí (làm tròn theo quy tắc IELTS: .25 lên .5, .75 lên 1.0).
    3. Feedback (Quan trọng):
       - Phải viết bằng TIẾNG VIỆT, giọng văn xây dựng, khuyến khích.
       - Cấu trúc bắt buộc trong feedback:
         + Phần trả lời của thí sinh: ...
         + Nhận xét chung: ...
         + Điểm mạnh: ...
         + Điểm yếu: (Chỉ ra lỗi sai cụ thể trong bài nếu có và cách sửa)
         + Nhận xét 4 tiêu chí
    """,
                section, topic);
    }

}