package com.loopers.interfaces.api.cart;


import com.loopers.application.cart.CartCommand;
import com.loopers.application.cart.CartFacade;
import com.loopers.application.cart.CartInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/carts")
public class CartController implements CartV1ApiSpec {

    private final CartFacade cartFacade;

    @PostMapping("")
    @Override
    public ApiResponse<CartV1Dto.CartResponse> addToCart(
            @RequestBody CartV1Dto.CartRequest request,
            @RequestHeader("X-USER-ID") String userId
    ) {
        // request -> command
        CartCommand command = request.toCommand(userId);
        // facade
        CartInfo info = cartFacade.addToCart(command);
        // info -> response
        if (info == null) {
            return ApiResponse.success(CartV1Dto.CartResponse.fail());
        }
        return ApiResponse.success(CartV1Dto.CartResponse.success());
    }

    @GetMapping("")
    @Override
    public ApiResponse<CartV1Dto.CartItemListResponse> getMyCart(
            @RequestHeader("X-USER-ID") String userId
    ) {
        // facade
        CartInfo info = cartFacade.getMyCart(userId);
        // info -> response
        CartV1Dto.CartItemListResponse response = CartV1Dto.CartItemListResponse.from(info);

        return ApiResponse.success(response);
    }

}
