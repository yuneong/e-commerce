package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @PostMapping("")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> placeOrder(
            @RequestBody OrderV1Dto.OrderRequest request,
            @RequestHeader("X-USER-ID") String userId
    ) {
        // request -> command
        OrderCommand command = request.toCommand(userId);
        // facade
        OrderInfo info = orderFacade.placeOrder(command);
        // info -> response
        OrderV1Dto.OrderResponse response = OrderV1Dto.OrderResponse.from(info);

        return ApiResponse.success(response);
    }


    @GetMapping("")
    @Override
    public ApiResponse<OrderV1Dto.OrderListResponse> getOrders(
            @RequestHeader("X-USER-ID") String userId
    ) {
        // facade
        List<OrderInfo> infos = orderFacade.getOrders(userId);
        // info -> response
        OrderV1Dto.OrderListResponse response = OrderV1Dto.OrderListResponse.from(infos);

        return ApiResponse.success(response);
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> getOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-USER-ID") String userId
    ) {
        // facade
        OrderInfo info = orderFacade.getOrderDetail(orderId, userId);
        // info -> response
        OrderV1Dto.OrderResponse response = OrderV1Dto.OrderResponse.from(info);

        return ApiResponse.success(response);
    }

}
