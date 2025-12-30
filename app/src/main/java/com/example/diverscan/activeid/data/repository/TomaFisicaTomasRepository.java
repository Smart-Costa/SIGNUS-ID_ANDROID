package com.example.diverscan.activeid.data.repository;

import android.content.Context;

import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;

import java.util.List;

public class TomaFisicaTomasRepository {
    private final TomaFisicaTomasDao dao;

    public TomaFisicaTomasRepository(Context context) { this.dao = new TomaFisicaTomasDao(context); }

    public void syncFromApi(String tomaFisicaId, Runnable onComplete) {
        dao.fetchAndSyncFromApi(tomaFisicaId, onComplete);
    }

    public void sync(List<TomaFisicaTomasEntity> data) {
        dao.syncResumen(data);
    }

    public List<TomaFisicaTomasEntity> getPendientes(String tomaFisicaId) {
        if (tomaFisicaId == null || tomaFisicaId.isEmpty()) {
            return dao.getAll();
        }
        return dao.getByTomaFisicaId(tomaFisicaId);
    }

    public List<TomaFisicaTomasEntity> getPendientes() {
        return dao.getAll();
    }

    public List<TomaFisicaTomasEntity> getCompletas() {
        // TODO: Implement logic to filter completed items if applicable
        return new java.util.ArrayList<>();
    }
}
