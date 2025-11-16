package com.example.shopmohinh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity extends AbtractEntity{
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_main")
    private boolean isMainImage;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "Id")
    private Product product;
}
