package com.example.diverscan.activeid.data.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.example.diverscan.activeid.data.local.entity.UbicacionEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class UbicacionRepository {
    public void getUbicaciones(Context context, MutableLiveData<List<UbicacionEntity>> liveData) {
        Type listType = new TypeToken<List<UbicacionEntity>>(){}.getType();
        ApiClient.getInstance(context).get("GetUbicacionesHH", listType, new ApiCallback<List<UbicacionEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<UbicacionEntity>> response) {
                if (response.success && response.data != null) {
                    liveData.postValue(response.data);
                } else {
                    liveData.postValue(null);
                }
            }
        });
    }
}
