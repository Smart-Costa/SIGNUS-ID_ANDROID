package com.example.diverscan.activeid.data.remote.response;

public class ApiResponse<T> {
    public final boolean success;
    public final T data;
    public final String errorMessage;
    public final int statusCode;
    public ApiResponse(boolean success, T data, String errorMessage, int statusCode) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }
    public static <T> ApiResponse<T> success(T data, int statusCode) {
        return new ApiResponse<>(true, data, null, statusCode);
    }
    public static <T> ApiResponse<T> failure(String errorMessage, int statusCode) {
        return new ApiResponse<>(false, null, errorMessage, statusCode);
    }
}
