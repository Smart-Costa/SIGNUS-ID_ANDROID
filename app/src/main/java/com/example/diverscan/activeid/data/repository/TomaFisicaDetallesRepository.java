package com.example.diverscan.activeid.data.repository;

import android.content.Context;

import com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;

import java.util.List;

public class TomaFisicaDetallesRepository {
    private final TomaFisicaDetallesDao dao;

    public TomaFisicaDetallesRepository(Context context) { this.dao = new TomaFisicaDetallesDao(context); }

    public void sync(List<TomaFisicaDetallesEntity> data) {
        dao.syncDetalle(data);
    }
}
