package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCommand;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/callback")
    public ApiResponse<Object> paymentCallback(
            @RequestBody PaymentV1Dto.PaymentRequest request
    ) {
        PaymentCommand command = PaymentV1Dto.PaymentRequest.toCommand(request);
        paymentFacade.paymentCallback(command);

        return ApiResponse.success();
    }

}
