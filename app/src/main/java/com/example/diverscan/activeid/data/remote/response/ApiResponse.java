package com.example.diverscan.activeid.data.remote.response;

public class ApiResponse<T> {
    public final boolean success;
    public final T data;
    public final String errorMessage;
    public final int statusCode;
    public final int totalPages;

    public ApiResponse(boolean success, T data, String errorMessage, int statusCode, int totalPages) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
        this.totalPages = totalPages;
    }
    public static <T> ApiResponse<T> success(T data, int statusCode) {
        return new ApiResponse<>(true, data, null, statusCode, 0);
    }
    public static <T> ApiResponse<T> success(T data, int statusCode, int totalPages) {
        return new ApiResponse<>(true, data, null, statusCode, totalPages);
    }
    public static <T> ApiResponse<T> failure(String errorMessage, int statusCode) {
        return new ApiResponse<>(false, null, errorMessage, statusCode, 0);
    }
}
