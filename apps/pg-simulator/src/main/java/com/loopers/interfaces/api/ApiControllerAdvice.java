package com.loopers.interfaces.api;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<?>> handle(CoreException e) {
        log.warn("CoreException : {}", e.getCustomMessage() != null ? e.getCustomMessage() : e.getMessage(), e);
        return failureResponse(e.getErrorType(), e.getCustomMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(MethodArgumentTypeMismatchException e) {
        String name = e.getName();
        String type = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
        Object value = e.getValue() != null ? e.getValue() : "null";
        String message = String.format("요청 파라미터 '%s' (타입: %s)의 값 '%s'이(가) 잘못되었습니다.", name, type, value);
        return failureResponse(ErrorType.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(MissingServletRequestParameterException e) {
        String name = e.getParameterName();
        String type = e.getParameterType();
        String message = String.format("필수 요청 파라미터 '%s' (타입: %s)가 누락되었습니다.", name, type);
        return failureResponse(ErrorType.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(HttpMessageNotReadableException e) {
        String errorMessage;
        Throwable rootCause = e.getRootCause();
        switch (rootCause) {
            case InvalidFormatException ex -> {
                String fieldName = ex.getPath().stream()
                        .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "?")
                        .reduce((a, b) -> a + "." + b).orElse("?");
                String valueIndicationMessage = "";
                if (ex.getTargetType().isEnum()) {
                    Object[] enumValues = ex.getTargetType().getEnumConstants();
                    String enums = "";
                    if (enumValues != null) {
                        enums = String.join(", ", java.util.Arrays.stream(enumValues).map(Object::toString).toArray(String[]::new));
                    }
                    valueIndicationMessage = "사용 가능한 값 : [" + enums + "]";
                }
                String expectedType = ex.getTargetType().getSimpleName();
                Object value = ex.getValue();
                errorMessage = String.format("필드 '%s'의 값 '%s'이(가) 예상 타입(%s)과 일치하지 않습니다. %s", fieldName, value, expectedType, valueIndicationMessage);
            }
            case MismatchedInputException ex -> {
                String fieldPath = ex.getPath().stream()
                        .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "?")
                        .reduce((a, b) -> a + "." + b).orElse("?");
                errorMessage = String.format("필수 필드 '%s'이(가) 누락되었습니다.", fieldPath);
            }
            case JsonMappingException ex -> {
                String fieldPath = ex.getPath().stream()
                        .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "?")
                        .reduce((a, b) -> a + "." + b).orElse("?");
                errorMessage = String.format("필드 '%s'에서 JSON 매핑 오류가 발생했습니다: %s", fieldPath, ex.getOriginalMessage());
            }
            case null, default -> errorMessage = "요청 본문을 처리하는 중 오류가 발생했습니다. JSON 메세지 규격을 확인해주세요.";
        }
        return failureResponse(ErrorType.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(ServerWebInputException e) {
        String missingParams = extractMissingParameter(e.getReason() != null ? e.getReason() : "");
        if (!missingParams.isEmpty()) {
            return failureResponse(ErrorType.BAD_REQUEST, String.format("필수 요청 값 '%s'가 누락되었습니다.", missingParams));
        } else {
            return failureResponse(ErrorType.BAD_REQUEST, null);
        }
    }

    private String extractMissingParameter(String message) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("'(.+?)'").matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(NoResourceFoundException e) {
        return failureResponse(ErrorType.NOT_FOUND, null);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiResponse<?>> handle(Throwable e) {
        log.error("Exception : {}", e.getMessage(), e);
        return failureResponse(ErrorType.INTERNAL_ERROR, null);
    }

    private ResponseEntity<ApiResponse<?>> failureResponse(ErrorType errorType, String errorMessage) {
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.fail(errorType.getCode(), errorMessage != null ? errorMessage : errorType.getMessage()));
    }
}
