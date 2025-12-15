package com.example.diverscan.activeid.data.repository;

import android.content.Context;

import com.example.diverscan.activeid.data.local.dao.UbicacionDao;
import com.example.diverscan.activeid.data.local.entity.UbicacionEntity;

import java.util.List;

public class UbicacionRepository {

    private final UbicacionDao dao;

    public UbicacionRepository(Context context) {
        dao = new UbicacionDao(context);
    }

    public List<UbicacionEntity> getUbicaciones() {
        return dao.getAllUbicaciones();
    }
}
