package com.example.diverscan.activeid.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.ActivoFotoEntity;
import com.example.diverscan.activeid.data.local.entity.CategoriaEntity;
import com.example.diverscan.activeid.data.local.entity.EmpresaEntity;
import com.example.diverscan.activeid.data.local.entity.EstadoEntity;
import com.example.diverscan.activeid.data.local.entity.MarcaEntity;
import com.example.diverscan.activeid.data.local.entity.ModeloEntity;
import com.example.diverscan.activeid.data.local.entity.UbicacionSecundariaEntity;
import com.example.diverscan.activeid.data.remote.api.ApiClient;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ActivoRepository {

    public void obtenerCatalogos(Context context,
                                 MutableLiveData<List<CategoriaEntity>> categorias,
                                 MutableLiveData<List<EstadoEntity>> estados,
                                 MutableLiveData<List<EmpresaEntity>> empresas,
                                 MutableLiveData<List<MarcaEntity>> marcas,
                                 MutableLiveData<List<ModeloEntity>> modelos,
                                 MutableLiveData<List<UbicacionSecundariaEntity>> ubicacionesS) {

        Type catType = new TypeToken<List<CategoriaEntity>>() {}.getType();
        ApiClient.getInstance(context).get("ActivosDetail/Categorias", catType, new ApiCallback<List<CategoriaEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<CategoriaEntity>> response) {
                categorias.postValue(response.success ? response.data : null);
            }
        });

        Type estType = new TypeToken<List<EstadoEntity>>() {}.getType();
        ApiClient.getInstance(context).get("ActivosDetail/Estados", estType, new ApiCallback<List<EstadoEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<EstadoEntity>> response) {
                estados.postValue(response.success ? response.data : null);
            }
        });

        Type empType = new TypeToken<List<EmpresaEntity>>() {}.getType();
        ApiClient.getInstance(context).get("ActivosDetail/Empresas", empType, new ApiCallback<List<EmpresaEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<EmpresaEntity>> response) {
                empresas.postValue(response.success ? response.data : null);
            }
        });

        Type marType = new TypeToken<List<MarcaEntity>>() {}.getType();
        ApiClient.getInstance(context).get("ActivosDetail/Marcas", marType, new ApiCallback<List<MarcaEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<MarcaEntity>> response) {
                marcas.postValue(response.success ? response.data : null);
            }
        });

        Type modType = new TypeToken<List<ModeloEntity>>() {}.getType();
        ApiClient.getInstance(context).get("ActivosDetail/Modelos", modType, new ApiCallback<List<ModeloEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<ModeloEntity>> response) {
                modelos.postValue(response.success ? response.data : null);
            }
        });

        Type ubiSType = new TypeToken<List<UbicacionSecundariaEntity>>() {}.getType();
        ApiClient.getInstance(context).get("ActivosDetail/UbicacionesS", ubiSType, new ApiCallback<List<UbicacionSecundariaEntity>>() {
            @Override
            public void onComplete(ApiResponse<List<UbicacionSecundariaEntity>> response) {
                ubicacionesS.postValue(response.success ? response.data : null);
            }
        });
    }

    public void obtenerUbicacionesSecundarias(Context context, ApiCallback<List<UbicacionSecundariaEntity>> callback) {
        Type ubiSType = new TypeToken<List<UbicacionSecundariaEntity>>() {}.getType();
        ApiClient.getInstance(context).get("ActivosDetail/UbicacionesS", ubiSType, callback);
    }

    // ✅ Guardar el activo en backend o local
    public void guardarActivo(Context context, ActivoEntity activo) {
    }

    // ✅ Guardar RFID y fotos
    public void guardarTagYFotos(Context context, String ubicacionSecundaria, String rfid, List<ActivoFotoEntity> fotos) {
        Log.d("ActivoRepository",
                "Guardando Tag RFID: " + rfid +
                        " | Ubicación secundaria: " + ubicacionSecundaria +
                        " | Cantidad de fotos: " + fotos.size());
    }
}
