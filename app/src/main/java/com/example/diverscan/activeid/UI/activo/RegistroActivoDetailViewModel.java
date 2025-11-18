package com.example.diverscan.activeid.UI.activo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.CategoriaEntity;
import com.example.diverscan.activeid.data.local.entity.EmpresaEntity;
import com.example.diverscan.activeid.data.local.entity.EstadoEntity;
import com.example.diverscan.activeid.data.local.entity.MarcaEntity;
import com.example.diverscan.activeid.data.local.entity.ModeloEntity;
import com.example.diverscan.activeid.data.local.entity.UbicacionSecundariaEntity;
import com.example.diverscan.activeid.data.repository.ActivoRepository;

import java.util.ArrayList;
import java.util.List;

public class RegistroActivoDetailViewModel extends AndroidViewModel {
    private final ActivoRepository repository;

    private final MutableLiveData<List<CategoriaEntity>> categoriasEntity = new MutableLiveData<>();
    private final MutableLiveData<List<EstadoEntity>> estadosEntity = new MutableLiveData<>();
    private final MutableLiveData<List<EmpresaEntity>> empresasEntity = new MutableLiveData<>();
    private final MutableLiveData<List<MarcaEntity>> marcasEntity = new MutableLiveData<>();
    private final MutableLiveData<List<ModeloEntity>> modelosEntity = new MutableLiveData<>();
    private final MutableLiveData<List<UbicacionSecundariaEntity>> ubicacionesSecundariasEntity = new MutableLiveData<>();

    private final MutableLiveData<List<String>> categorias = new MutableLiveData<>();
    private final MutableLiveData<List<String>> estados = new MutableLiveData<>();
    private final MutableLiveData<List<String>> empresas = new MutableLiveData<>();
    private final MutableLiveData<List<String>> marcas = new MutableLiveData<>();
    private final MutableLiveData<List<String>> modelos = new MutableLiveData<>();
    private final MutableLiveData<List<String>> ubicacionesSecundarias = new MutableLiveData<>();

    public RegistroActivoDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new ActivoRepository();
    }

    public MutableLiveData<List<String>> getCategorias() { return categorias; }
    public MutableLiveData<List<String>> getEstados() { return estados; }
    public MutableLiveData<List<String>> getEmpresas() { return empresas; }
    public MutableLiveData<List<String>> getMarcas() { return marcas; }
    public MutableLiveData<List<String>> getModelos() { return modelos; }
    public MutableLiveData<List<String>> getUbicacionesSecundarias() { return ubicacionesSecundarias; }

    public void cargarCatalogos(Context context) {
        repository.obtenerCatalogos(context,
                categoriasEntity,
                estadosEntity,
                empresasEntity,
                marcasEntity,
                modelosEntity,
                ubicacionesSecundariasEntity);

        categoriasEntity.observeForever(lista -> categorias.postValue(mapearCategorias(lista)));
        estadosEntity.observeForever(lista -> estados.postValue(mapearEstados(lista)));
        empresasEntity.observeForever(lista -> empresas.postValue(mapearEmpresas(lista)));
        marcasEntity.observeForever(lista -> marcas.postValue(mapearMarcas(lista)));
        modelosEntity.observeForever(lista -> modelos.postValue(mapearModelos(lista)));
        ubicacionesSecundariasEntity.observeForever(lista -> ubicacionesSecundarias.postValue(mapearUbicacionesSec(lista)));
    }

    public void guardarActivo(Context context, ActivoEntity activo) {
        repository.guardarActivo(context, activo);
        Toast.makeText(context, "Activo guardado correctamente", Toast.LENGTH_SHORT).show();
    }

    private List<String> mapearCategorias(List<CategoriaEntity> lista) {
        List<String> nombres = new ArrayList<>();
        if (lista != null)
            for (CategoriaEntity item : lista)
                if (item.getCategoria() != null)
                    nombres.add(item.getCategoria());
        return nombres;
    }

    private List<String> mapearEstados(List<EstadoEntity> lista) {
        List<String> nombres = new ArrayList<>();
        if (lista != null)
            for (EstadoEntity item : lista)
                if (item.getEstado() != null)
                    nombres.add(item.getEstado());
        return nombres;
    }

    private List<String> mapearEmpresas(List<EmpresaEntity> lista) {
        List<String> nombres = new ArrayList<>();
        if (lista != null)
            for (EmpresaEntity item : lista)
                if (item.getEmpresa() != null)
                    nombres.add(item.getEmpresa());
        return nombres;
    }

    private List<String> mapearMarcas(List<MarcaEntity> lista) {
        List<String> nombres = new ArrayList<>();
        if (lista != null)
            for (MarcaEntity item : lista)
                if (item.getMarca() != null)
                    nombres.add(item.getMarca());
        return nombres;
    }

    private List<String> mapearModelos(List<ModeloEntity> lista) {
        List<String> nombres = new ArrayList<>();
        if (lista != null)
            for (ModeloEntity item : lista)
                if (item.getModelo() != null)
                    nombres.add(item.getModelo());
        return nombres;
    }

    private List<String> mapearUbicacionesSec(List<UbicacionSecundariaEntity> lista) {
        List<String> nombres = new ArrayList<>();
        if (lista != null)
            for (UbicacionSecundariaEntity item : lista)
                if (item.getUbicacionS() != null)
                    nombres.add(item.getUbicacionS());
        return nombres;
    }
}
