package com.example.diverscan.activeid.data.remote.response;

import java.util.List;

public class NovedadesResponse {
    public boolean ok;
    public NovedadesData data;

    public static class NovedadesData {
        public boolean tieneAsignacion;
        public String tomaFisicaId;
        public int cantidadTareasPendientes;
        public List<ActivoReubicacionDto> activosParaReubicar;
    }
}
