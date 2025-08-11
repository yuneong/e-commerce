package com.loopers.domain.cart;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "cart_items")
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    private int price;

    public static CartItem create(Product product, int quantity, int price) {
        CartItem cartItem = new CartItem();

        cartItem.validate(product, quantity, price);

        cartItem.product = product;
        cartItem.quantity = quantity;
        cartItem.price = price;

        return cartItem;
    }

    public void validate(Product product, int quantity, int price) {
        if (product == null) {
            throw new NullPointerException("상품은 필수입니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("상품 수량은 0보다 커야 합니다.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
    }

}
