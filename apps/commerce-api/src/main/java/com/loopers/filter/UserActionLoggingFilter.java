package com.loopers.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class UserActionLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(req);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(res);

        // traceId 생성
//        String traceId = req.getHeader("X-Request-Id");
//        if (traceId == null || traceId.isEmpty()) {
//            traceId = UUID.randomUUID().toString();
//        }
//        MDC.put("traceId", traceId);

        // userId 추출
        String userId = req.getHeader("X-USER-ID");
        if (userId != null && !userId.isEmpty()) {
            MDC.put("userId", userId);
        }

        // action 추출
        String action = extractAction(req);

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 요청 파라미터/바디
            String query = req.getQueryString();
            byte[] reqBuf = requestWrapper.getContentAsByteArray();
            String requestBody = reqBuf.length > 0 ? new String(reqBuf, StandardCharsets.UTF_8) : "";

            // 응답 바디
            byte[] resBuf = responseWrapper.getContentAsByteArray();
            String responseBody = resBuf.length > 0 ? new String(resBuf, StandardCharsets.UTF_8) : "";

            // logging
//            log.info(
//                    "[actionUri={}] [query={}] [requestBody={}] [responseBody={}]",
//                    action, query, requestBody, responseBody
//            );

            // 반드시 response를 복구해서 실제 클라이언트로 flush
            // 하지 않으면 응답이 클라이언트로 전달되지 않음
            responseWrapper.copyBodyToResponse();

//            MDC.remove("traceId");
            MDC.remove("userId");
        }

    }

    private String extractAction(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String method = req.getMethod();
        return method + " " + uri;
    }

}
