package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentApplicationService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.domain.user.UserInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentApi {

    private final PaymentApplicationService paymentApplicationService;

    public PaymentApi(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping
    public ApiResponse<PaymentDto.TransactionResponse> request(
            UserInfo userInfo,
            @RequestBody PaymentDto.PaymentRequest request
    ) {
        request.validate();

        // 100ms ~ 500ms 지연
        try {
            long delay = 100 + (long) (Math.random() * 401);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 40% 확률로 요청 실패
        int random = 1 + (int) (Math.random() * 100);
        if (random <= 40) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "현재 서버가 불안정합니다. 잠시 후 다시 시도해주세요.");
        }

        return ApiResponse.success(
                PaymentDto.TransactionResponse.from(
                        paymentApplicationService.createTransaction(request.toCommand(userInfo.userId()))
                )
        );
    }

    @GetMapping("/{transactionKey}")
    public ApiResponse<PaymentDto.TransactionDetailResponse> getTransaction(
            UserInfo userInfo,
            @PathVariable("transactionKey") String transactionKey
    ) {
        return ApiResponse.success(
                PaymentDto.TransactionDetailResponse.from(
                        paymentApplicationService.getTransactionDetailInfo(userInfo, transactionKey)
                )
        );
    }

    @GetMapping
    public ApiResponse<PaymentDto.OrderResponse> getTransactionsByOrder(
            UserInfo userInfo,
            @RequestParam(value = "orderId", required = false) Long orderId
    ) {
        return ApiResponse.success(
                PaymentDto.OrderResponse.from(
                        paymentApplicationService.findTransactionsByOrderId(userInfo, orderId)
                )
        );
    }
}
