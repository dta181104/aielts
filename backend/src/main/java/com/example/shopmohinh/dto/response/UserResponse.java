package com.example.shopmohinh.dto.response;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//Access ModiFier
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
     Long id;

     String code;

     LocalDateTime createdDate;

     LocalDateTime updatedDate;

     String createdBy;

     String updatedBy;

     String name;

     Boolean sex;

     String address;

     String phone;

     String email;

     String status;

     LocalDate date;

     String username;

     Boolean deleted;

     String avatar;

     Set<RoleResponse> roles;

}
