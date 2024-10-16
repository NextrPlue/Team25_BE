package com.team25.backend.controller;

import com.team25.backend.dto.CustomUserDetails;
import com.team25.backend.dto.request.BillingKeyRequest;
import com.team25.backend.dto.request.PaymentRequest;
import com.team25.backend.dto.request.ExpireBillingKeyRequest;
import com.team25.backend.dto.response.ApiResponse;
import com.team25.backend.dto.response.BillingKeyResponse;
import com.team25.backend.dto.response.PaymentResponse;
import com.team25.backend.dto.response.ExpireBillingKeyResponse;
import com.team25.backend.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 빌링키 발급
    @PostMapping("/billing-key")
    public ResponseEntity<ApiResponse<BillingKeyResponse>> createBillingKey(@RequestBody BillingKeyRequest requestDto) throws Exception {
        String userId = getCurrentUserId(); // 현재 사용자 식별자 가져오기
        BillingKeyResponse responseDto = paymentService.createBillingKey(requestDto, userId);
        return new ResponseEntity<>(
                new ApiResponse<>(true, "빌링키 발급을 성공했습니다.", responseDto), HttpStatus.OK
        );
    }

    // 결제 요청
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> payment(@RequestBody PaymentRequest requestDto) throws Exception {
        String userId = getCurrentUserId();
        PaymentResponse responseDto = paymentService.requestPayment(userId, requestDto);
        return new ResponseEntity<>(
                new ApiResponse<>(true, responseDto.resultMsg(), responseDto), HttpStatus.OK
        );
    }

    // 빌링키 삭제
    @PostMapping("/billing-key/expire")
    public ResponseEntity<ApiResponse<ExpireBillingKeyResponse>> expireBillingKey(@RequestBody ExpireBillingKeyRequest requestDto) throws Exception {
        String userId = getCurrentUserId();
        ExpireBillingKeyResponse responseDto = paymentService.expireBillingKey(userId, requestDto);
        return new ResponseEntity<>(
                new ApiResponse<>(true, responseDto.resultMsg(), responseDto), HttpStatus.OK
        );
    }

    // 빌링키 존재 유무 확인
    @GetMapping("/billing-key/exists")
    public ResponseEntity<ApiResponse<Boolean>> billingKeyExists() {
        String userId = getCurrentUserId();
        boolean exists = paymentService.billingKeyExists(userId);
        return new ResponseEntity<>(
                new ApiResponse<>(true, "성공적으로 빌링키 존재 유무를 가져왔습니다.", exists), HttpStatus.OK
        );
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("사용자가 인증되지 않았습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUser) {
            return customUser.getUsername(); // UUID로 수정 필요
        } else {
            throw new RuntimeException("인증 정보가 CustomUserDetails 타입이 아닙니다.");
        }
    }
}
