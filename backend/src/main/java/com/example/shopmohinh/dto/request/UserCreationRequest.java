package com.example.shopmohinh.dto.request;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//Access ModiFier
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {

    String code;

    @NotNull(message = "Name cannot be empty")
    String name;

    Boolean sex;

    String address;

    String phone;

    @NotNull(message = "Email cannot be empty")
    String email;

    String status;

    LocalDate date;

    @Size(min = 3,message = "USERNAME_INVALID")
    String username;

    //validate password
    @Size(min = 5,message = "PASSWORD_INVALID")
    String pass;

    LocalDateTime createdDate;

    String createdBy;

    MultipartFile avatarFile;

    @Pattern(regexp = "^(https?|ftp)://[^\s/$.?#].[^\s]*$", message = "AVATAR_URL_INVALID")
    String avatar;

    private List<String> roles;
}
