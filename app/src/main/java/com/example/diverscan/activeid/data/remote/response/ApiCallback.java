package com.example.diverscan.activeid.data.remote.response;

public interface ApiCallback<T> {
    void onComplete(ApiResponse<T> response);
}
