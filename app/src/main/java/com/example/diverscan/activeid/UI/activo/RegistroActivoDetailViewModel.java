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

    private final MutableLiveData<List<ComboItem>> categorias = new MutableLiveData<>();
    private final MutableLiveData<List<ComboItem>> estados = new MutableLiveData<>();
    private final MutableLiveData<List<ComboItem>> empresas = new MutableLiveData<>();
    private final MutableLiveData<List<ComboItem>> marcas = new MutableLiveData<>();
    private final MutableLiveData<List<ComboItem>> modelos = new MutableLiveData<>();
    private final MutableLiveData<List<ComboItem>> ubicacionesSecundarias = new MutableLiveData<>();

    public RegistroActivoDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new ActivoRepository();
    }

    public MutableLiveData<List<ComboItem>> getCategorias() { return categorias; }
    public MutableLiveData<List<ComboItem>> getEstados() { return estados; }
    public MutableLiveData<List<ComboItem>> getEmpresas() { return empresas; }
    public MutableLiveData<List<ComboItem>> getMarcas() { return marcas; }
    public MutableLiveData<List<ComboItem>> getModelos() { return modelos; }
    public MutableLiveData<List<ComboItem>> getUbicacionesSecundarias() { return ubicacionesSecundarias; }

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

    private List<ComboItem> mapearCategorias(List<CategoriaEntity> lista) {
        List<ComboItem> items = new ArrayList<>();
        if (lista != null)
            for (CategoriaEntity item : lista)
                if (item.getCategoria() != null)
                    items.add(new ComboItem(item.getCSysId(), item.getCategoria()));
        return items;
    }

    private List<ComboItem> mapearEstados(List<EstadoEntity> lista) {
        List<ComboItem> items = new ArrayList<>();
        if (lista != null)
            for (EstadoEntity item : lista)
                if (item.getEstado() != null)
                    items.add(new ComboItem(item.getESysId(), item.getEstado()));
        return items;
    }

    private List<ComboItem> mapearEmpresas(List<EmpresaEntity> lista) {
        List<ComboItem> items = new ArrayList<>();
        if (lista != null)
            for (EmpresaEntity item : lista)
                if (item.getEmpresa() != null)
                    items.add(new ComboItem(item.getESysId(), item.getEmpresa()));
        return items;
    }

    private List<ComboItem> mapearMarcas(List<MarcaEntity> lista) {
        List<ComboItem> items = new ArrayList<>();
        if (lista != null)
            for (MarcaEntity item : lista)
                if (item.getMarca() != null)
                    items.add(new ComboItem(item.getMSysId(), item.getMarca()));
        return items;
    }

    private List<ComboItem> mapearModelos(List<ModeloEntity> lista) {
        List<ComboItem> items = new ArrayList<>();
        if (lista != null)
            for (ModeloEntity item : lista)
                if (item.getModelo() != null)
                    items.add(new ComboItem(item.getMSysId(), item.getModelo()));
        return items;
    }

    private List<ComboItem> mapearUbicacionesSec(List<UbicacionSecundariaEntity> lista) {
        List<ComboItem> items = new ArrayList<>();
        if (lista != null)
            for (UbicacionSecundariaEntity item : lista)
                if (item.getUbicacionS() != null)
                    items.add(new ComboItem(item.getUSSysId(), item.getUbicacionS()));
        return items;
    }

    public static class ComboItem {
        private final String id;
        private final String nombre;

        public ComboItem(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }

        @NonNull
        @Override
        public String toString() {
            return nombre;
        }
    }
}
