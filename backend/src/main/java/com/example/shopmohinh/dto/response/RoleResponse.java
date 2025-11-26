package com.example.shopmohinh.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    Long id;

    String code;

    String name;

    String status;

    Set<PermissionResponse> permissions;

    LocalDateTime createdDate;

    LocalDateTime updatedDate;

    String createdBy;

    String updatedBy;

    Boolean deleted;

}
