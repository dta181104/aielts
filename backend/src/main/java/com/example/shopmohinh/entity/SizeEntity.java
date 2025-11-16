package com.example.shopmohinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "size")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeEntity extends AbtractEntity{
    @Column(name = "CODE")
    private String code;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "NAME")
    private String name;
    @Column(name = "DESCRIPTION")
    private String description;
}
