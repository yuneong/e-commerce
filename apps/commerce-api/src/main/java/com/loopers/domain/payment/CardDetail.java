package com.loopers.domain.payment;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardDetail {
    private String cardNo;
    private CardType cardType;
    private String transactionKey;

    public CardDetail withTransactionKey(String transactionKey) {
        return new CardDetail(
                this.cardNo,
                this.cardType,
                transactionKey
        );
    }
}
