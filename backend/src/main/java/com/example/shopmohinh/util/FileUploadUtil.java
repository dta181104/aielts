package com.example.shopmohinh.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
@Service
public class FileUploadUtil {
    private final Cloudinary cloudinary;

    // code cũ (không dùng)
    public String uploadFile(MultipartFile avatarFile) {
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                return checkFile(avatarFile);
            } catch (IOException e) {
                log.error("Error uploading file: {}", e.getMessage());
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }
        log.warn("No avatar file provided, using default avatar URL.");
        return "https://asset.cloudinary.com/dvxobkvcx/ec27e05c5476c3c95ce0d4cc48841456";
    }

    private String checkFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto", "folder", "avatars"));
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            log.error("Upload file failed: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // code mới
    // --- PUBLIC METHODS ---

    // Hàm upload ảnh (Avatar)
    public String uploadAvatar(MultipartFile avatarFile, String customFileName) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            log.warn("No avatar file provided, using default avatar URL.");
            return "https://asset.cloudinary.com/dvxobkvcx/ec27e05c5476c3c95ce0d4cc48841456";
        }

        return uploadGenericFile(avatarFile, "image/", "AIELTS/avatars", customFileName);
    }

    // Hàm upload Audio
    public String uploadAudio(MultipartFile audioFile, String folderName, String customFileName) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return uploadGenericFile(audioFile, "audio/", folderName, customFileName);
    }

    // Hàm up load nhều file audio
    public List<String> uploadMultipleAudio(List<MultipartFile> audioFiles, String folderName, String baseFileName) {
        List<String> audioUrls = new ArrayList<>();

        // Kiểm tra danh sách rỗng
        if (audioFiles == null || audioFiles.isEmpty()) {
            return audioUrls; // Trả về list rỗng
        }

        // Duyệt từng file trong danh sách
        for (int i = 0; i < audioFiles.size(); i++) {
            MultipartFile file = audioFiles.get(i);

            // Xử lý tên file cho từng file
            String finalName = null;
            if (baseFileName != null && !baseFileName.trim().isEmpty()) {
                finalName = baseFileName + "_" + i;
            }

            try {
                String url = uploadGenericFile(file, "audio/", folderName, finalName);
                audioUrls.add(url);
            } catch (AppException e) {
                // Log lỗi và tiếp tục upload file tiếp theo
                log.error("Failed to upload file index {}: {}", i, e.getMessage());
            }
        }

        return audioUrls;
    }

    // --- PRIVATE HELPER ---

    /**
     * Hàm xử lý chung cho việc upload
     * @param file: File đầu vào
     * @param allowedTypePrefix: Tiền tố định dạng cho phép
     * @param folderName: Tên thư mục trên Cloudinary
     * @param customFileName: Tên file muốn đặt (không bao gồm đuôi file)
     */
    private String uploadGenericFile(MultipartFile file, String allowedTypePrefix, String folderName, String customFileName) {
        // 1. Validate Content Type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(allowedTypePrefix)) {
            log.error("Invalid file type. Expected: {} but got: {}", allowedTypePrefix, contentType);
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 2. Prepare upload parameters
        Map<String, Object> params = new HashMap<>();
        params.put("resource_type", "auto"); // Để auto cloudinary tự nhận diện là image hay video/audio
        params.put("folder", folderName);

        if (customFileName != null && !customFileName.trim().isEmpty()) {
            params.put("public_id", customFileName);
            params.put("unique_filename", "false");
            params.put("overwrite", "true");
        }

        // 3. Upload to Cloudinary
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            log.error("Upload file failed: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
