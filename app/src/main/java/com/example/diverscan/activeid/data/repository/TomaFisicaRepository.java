package com.example.diverscan.activeid.data.repository;

import android.content.Context;

import com.example.diverscan.activeid.data.local.dao.TomaFisicaDao;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaEntity;

import java.util.List;

public class TomaFisicaRepository {

    private final TomaFisicaDao dao;

    public TomaFisicaRepository(Context context) { dao = new TomaFisicaDao(context); }

    public List<TomaFisicaEntity> getTomaFisica() {
        return dao.getAllTomasFisicas();
    }

    public List<TomaFisicaEntity> getTomaFisicaByName(String nombre) {
        return dao.getTomaFisicaByName(nombre);
    }

    public void sync(Runnable onSuccess) {
        dao.fetchAndSyncFromApi(onSuccess);
    }
}
