package com.example.diverscan.activeid.UI.activo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.diverscan.activeid.data.local.entity.UbicacionEntity;
import com.example.diverscan.activeid.data.repository.UbicacionRepository;

import java.util.List;

public class RegistroActivoUbicacionViewModel extends AndroidViewModel {
    private final UbicacionRepository repository;
    private final MutableLiveData<List<UbicacionEntity>> ubicaciones = new MutableLiveData<>();

    public RegistroActivoUbicacionViewModel(@NonNull Application application) {
        super(application);
        repository = new UbicacionRepository();
    }

    public LiveData<List<UbicacionEntity>> getUbicaciones() {
        return ubicaciones;
    }

    public void cargarUbicaciones() {
        repository.getUbicaciones(getApplication(), ubicaciones);
    }
}
