package com.example.shopmohinh.controller;

import com.example.shopmohinh.service.impl.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private VnpayService vnpayService;

    @PostMapping("/submitOrder")
    public ResponseEntity<String> submitOrder(
            @RequestParam("amount") int orderTotal,
            @RequestParam("orderInfo") String orderInfo,
            HttpServletRequest request) {

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String paymentUrl = vnpayService.createOrder(orderTotal, orderInfo, baseUrl);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/vnpay-payment")
    public ResponseEntity<Map<String, Object>> handlePaymentResponse(HttpServletRequest request) {
        int paymentStatus = vnpayService.orderReturn(request);
        Map<String, Object> response = new HashMap<>();
        response.put("paymentStatus", paymentStatus == 1 ? "success" : "fail");
        response.put("orderId", request.getParameter("vnp_OrderInfo"));
        response.put("totalPrice", request.getParameter("vnp_Amount"));
        response.put("paymentTime", request.getParameter("vnp_PayDate"));
        response.put("transactionId", request.getParameter("vnp_TransactionNo"));
        return ResponseEntity.ok(response);
    }
}
