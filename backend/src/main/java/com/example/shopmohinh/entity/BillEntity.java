package com.example.shopmohinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "bill")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillEntity extends AbtractEntity{
    @Column(name = "CODE")
    private String code;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "fee_ship")
    private Float feeShip;
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "phone")
    private String phone;
    @Column(name = "email")
    private String email;
    @Column(name = "note")
    private String note;
    @Column(name = "total_money")
    private Float totalMoney;

    @ManyToOne
    private PaymentEntity payment;

    @ManyToOne
    private User customer;

    @ManyToOne
    private User staff;
}
