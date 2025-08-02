package com.loopers.domain.cart;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "carts")
public class Cart extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "cart_id")
    private List<CartItem> cartItems = new ArrayList<>();

    private int totalPrice;

    public static Cart create(User user) {
        Cart cart = new Cart();

        cart.validate(user);

        cart.user = user;

        return cart;
    }

    public void validate(User user) {
        if (user == null) {
            throw new NullPointerException("장바구니를 생성할 때 사용자 정보는 필수입니다.");
        }
    }

    public void addItem(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("장바구니 아이템은 필수입니다.");
        }

        for (CartItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("장바구니 아이템은 null일 수 없습니다.");
            }
            cartItems.add(item);
            totalPrice += item.getPrice() * item.getQuantity();
        }
    }

}
