package com.example.shopmohinh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbtractEntity {
    @Column(name = "CODE")
    private String code;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "NAME")
    private String name;
    @Column(name = "SEX")
    private Boolean sex;
    @Column(name = "ADDRESS")
    private String address;
    @Column(name = "PHONE")
    private String phone;
    @Column(name = "EMAIL")
    private String email;
    @Column(name = "DATE")
    private LocalDate date;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "PASSWORD")
    private String pass;
    @Column(name = "AVATAR")
    private String avatar;

    @ManyToMany
    private Set<Role> roles;
}
