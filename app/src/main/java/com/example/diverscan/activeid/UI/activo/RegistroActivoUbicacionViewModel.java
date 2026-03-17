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
        repository = new UbicacionRepository(application.getApplicationContext());
    }

    public LiveData<List<UbicacionEntity>> getUbicaciones() {
        return ubicaciones;
    }

    public void cargarUbicaciones() {
        List<UbicacionEntity> lista = repository.getUbicaciones();
        android.util.Log.d("RegistroActivoUbicacionViewModel", "Cargando ubicaciones desde repositorio. Cantidad: " + lista.size());
        
        boolean dataIncompleta = false;
        if (!lista.isEmpty()) {
            // Si hay datos pero el nombre es nulo, la sync previa falló por mapeo
            if (lista.get(0).getUbicacionA() == null) {
                dataIncompleta = true;
                android.util.Log.d("RegistroActivoUbicacionViewModel", "Datos parecen incompletos (nombres nulos). Forzando sincronización.");
            }
        }

        if (lista.isEmpty() || dataIncompleta) {
            android.util.Log.d("RegistroActivoUbicacionViewModel", "Iniciando sincronización...");
            repository.syncUbicaciones(() -> {
                List<UbicacionEntity> nuevaLista = repository.getUbicaciones();
                android.util.Log.d("RegistroActivoUbicacionViewModel", "Sincronización completada. Nueva cantidad: " + nuevaLista.size());
                ubicaciones.postValue(nuevaLista);
            });
        } else {
            ubicaciones.setValue(lista);
        }
    }

    public void forzarSincronizacion() {
        android.util.Log.d("RegistroActivoUbicacionViewModel", "Forzando sincronización manual...");
        repository.syncUbicaciones(() -> {
            List<UbicacionEntity> nuevaLista = repository.getUbicaciones();
            android.util.Log.d("RegistroActivoUbicacionViewModel", "Sincronización manual completada. Nueva cantidad: " + nuevaLista.size());
            ubicaciones.postValue(nuevaLista);
        });
    }
}
