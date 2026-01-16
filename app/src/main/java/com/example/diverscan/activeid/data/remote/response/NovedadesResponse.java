package com.example.diverscan.activeid.data.remote.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NovedadesResponse {
    public boolean ok;
    public NovedadesData data;

    public static class NovedadesData {
        public boolean tieneAsignacion;
        public String tomaFisicaId;
        public int cantidadTareasPendientes;
        
        @SerializedName(value = "activosParaReubicar", alternate = {"ActivosParaReubicar"})
        public List<ActivoReubicacionDto> activosParaReubicar;
        
        @SerializedName(value = "tomas", alternate = {"Tomas"})
        public List<TomaNovedadDto> tomas;
    }
}
