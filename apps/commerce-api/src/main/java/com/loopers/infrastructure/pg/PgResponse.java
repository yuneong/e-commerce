package com.loopers.infrastructure.pg;

public record PgResponse(
        String transactionKey,
        String status,
        String reason
) {
}
