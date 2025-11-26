package com.example.shopmohinh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//Access ModiFier
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role extends AbtractEntity{
    @Column(name = "CODE")
    private String code;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "NAME")
    String name;

    @ManyToMany
    @OrderBy("id ASC")
    Set<Permission> permissions;
}
