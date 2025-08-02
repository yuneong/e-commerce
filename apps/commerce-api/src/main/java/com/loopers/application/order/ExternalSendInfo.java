package com.loopers.application.order;


public record ExternalSendInfo(
        boolean success,
        String message,
        String trackingNumber
) {

}
