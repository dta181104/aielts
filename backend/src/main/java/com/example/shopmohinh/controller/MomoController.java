package com.example.shopmohinh.controller;

import com.example.shopmohinh.service.impl.MomoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/momo")
@RestController
@RequiredArgsConstructor
public class MomoController {
    private final MomoService momoService;

    @PostMapping
    public String testPayment( @RequestParam("amount") String orderTotal,
                               @RequestParam("orderInfo") String orderInfo) {
        return momoService.createPaymentRequest(orderTotal, orderInfo);
    }

    @GetMapping("/order-status")
    public String checkPaymentStatus(@RequestBody String paymentId) {
        return momoService.checkPaymentStatus(paymentId);
    }
}
