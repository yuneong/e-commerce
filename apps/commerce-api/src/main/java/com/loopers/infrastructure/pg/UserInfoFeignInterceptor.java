package com.loopers.infrastructure.pg;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserInfoFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
//        String userInfo = "오윤영";
//        template.header("userInfo", userInfo);
    }

}
