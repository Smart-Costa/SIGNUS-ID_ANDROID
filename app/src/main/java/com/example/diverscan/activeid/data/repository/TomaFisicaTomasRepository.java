package com.example.diverscan.activeid.data.repository;

import android.content.Context;

import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;

import java.util.List;

public class TomaFisicaTomasRepository {
    private final TomaFisicaTomasDao dao;

    public TomaFisicaTomasRepository(Context context) { this.dao = new TomaFisicaTomasDao(context); }

    public void sync(List<TomaFisicaTomasEntity> data) {
        dao.syncResumen(data);
    }
}
