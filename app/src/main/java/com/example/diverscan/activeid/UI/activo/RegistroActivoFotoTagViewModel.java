package com.example.diverscan.activeid.UI.activo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.ActivoFotoEntity;
import com.example.diverscan.activeid.data.repository.ActivoRepository;

import java.util.ArrayList;
import java.util.List;

public class RegistroActivoFotoTagViewModel extends AndroidViewModel {

    private final ActivoRepository repository;
    private final MutableLiveData<List<String>> ubicacionesSecundarias = new MutableLiveData<>();

    public RegistroActivoFotoTagViewModel(@NonNull Application application) {
        super(application);
        repository = new ActivoRepository();
    }

    public MutableLiveData<List<String>> getUbicacionesSecundarias() {
        return ubicacionesSecundarias;
    }

    public void cargarUbicacionesSecundarias(Context context) {
        // Simulación temporal: esto luego vendrá del backend
        List<String> lista = new ArrayList<>();
        lista.add("Bodega Principal");
        lista.add("Taller Mantenimiento");
        lista.add("Zona Norte");
        lista.add("Zona Sur");
        ubicacionesSecundarias.postValue(lista);
    }

    public void guardarTagYFotos(Context context, String idActivo, String ubicacionSec, String rfid, List<ActivoFotoEntity> fotos) {
        // Aquí puedes implementar el guardado de datos
        // Ejemplo:
        for (ActivoFotoEntity foto : fotos) {
            foto.setIdActivo(idActivo);
            // guardar foto en la base de datos
        }

        // Guardar tag RFID y ubicación (puedes hacerlo con otro DAO o repositorio)
        Log.d("ViewModel", "Guardando tag y fotos para activo " + idActivo);
    }

    public void guardarActivoFinal(Context context, ActivoEntity activo) {
        ActivoDao dao = new ActivoDao(context);
        long res = dao.insertActivo(activo);

        if (res == -1) {
            Log.e("DAO", "Error al insertar activo");
        } else {
            Log.i("DAO", "Activo guardado con ID fila: " + res);
        }
    }
}
