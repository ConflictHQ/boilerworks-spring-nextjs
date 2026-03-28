package com.boilerworks.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean ok;
    private T data;
    private List<Map<String, Object>> errors;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> error(String field, String message) {
        return new ApiResponse<>(false, null, List.of(
            Map.of("field", field, "messages", List.of(message))
        ));
    }

    public static <T> ApiResponse<T> error(String message) {
        return error("__all__", message);
    }

    public static <T> ApiResponse<T> errors(List<Map<String, Object>> errors) {
        return new ApiResponse<>(false, null, errors);
    }
}
