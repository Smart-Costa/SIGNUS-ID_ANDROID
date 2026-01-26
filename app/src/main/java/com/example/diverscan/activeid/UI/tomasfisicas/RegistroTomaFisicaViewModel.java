package com.example.diverscan.activeid.UI.tomasfisicas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;
import com.example.diverscan.activeid.data.repository.TomaFisicaRepository;

import java.util.List;

public class RegistroTomaFisicaViewModel extends AndroidViewModel {

    private final TomaFisicaRepository repository;
    private final MutableLiveData<List<TomaFisicaEntity>> tomasfisicas = new MutableLiveData<>();

    public RegistroTomaFisicaViewModel(@NonNull Application application) {
        super(application);
        repository = new TomaFisicaRepository(application.getApplicationContext());
    }

    public LiveData<List<TomaFisicaEntity>> getTomasFisicas() {
        return tomasfisicas;
    }

    public void cargarTomasFisicas() {
        // Primero cargamos lo local
        tomasfisicas.setValue(repository.getTomaFisica());
        
        // COMENTADO POR SOLICITUD: La sincronización automática en la vista está deshabilitada.
        // Toda sincronización debe realizarse explícitamente a través del módulo de Sincronización.
        /*
        // Intentamos sincronizar
        repository.sync(() -> {
            // Si hay éxito, recargamos
            tomasfisicas.postValue(repository.getTomaFisica());
        });
        */
    }

    public void buscarTomasFisicasPorNombre(String nombre) {
        tomasfisicas.setValue(repository.getTomaFisicaByName(nombre));
    }
}
