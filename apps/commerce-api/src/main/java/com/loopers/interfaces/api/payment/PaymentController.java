package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.CallbackPaymentCommand;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentInfo;
import com.loopers.application.payment.ProcessPaymentCommand;
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
public class PaymentController implements PaymentV1ApiSpec {

    private final PaymentFacade paymentFacade;

    @PostMapping("")
    public ApiResponse<PaymentV1Dto.ProcessResponse> processPayment(
            @RequestBody PaymentV1Dto.ProcessRequest request
    ) {
        // request -> command
        ProcessPaymentCommand command = PaymentV1Dto.ProcessRequest.toCommand(request);
        // facade
        PaymentInfo info = paymentFacade.processPayment(command);

        PaymentV1Dto.ProcessResponse response = PaymentV1Dto.ProcessResponse.from(info);

        return ApiResponse.success(response);
    }


    @PostMapping("/callback")
    public ApiResponse<Object> paymentCallback(
            @RequestBody PaymentV1Dto.CallbackRequest request
    ) {
        CallbackPaymentCommand command = PaymentV1Dto.CallbackRequest.toCommand(request);
        paymentFacade.paymentCallback(command);

        return ApiResponse.success();
    }

}
