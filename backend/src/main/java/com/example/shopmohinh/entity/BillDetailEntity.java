package com.example.shopmohinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "bill_detail")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailEntity extends AbtractEntity{
    @Column(name = "CODE")
    private String code;
    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "price")
    private Float price;
    @Column(name = "STATUS")
    private String status;

    @ManyToOne
    private Product product;

    @ManyToOne
    private BillEntity bill;
}
