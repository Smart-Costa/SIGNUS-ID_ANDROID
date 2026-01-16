package com.example.diverscan.activeid.data.remote.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TomaNovedadDto {
    public String tomaFisicaId;
    @SerializedName(value = "nombreToma", alternate = {"NombreToma"})
    public String nombreToma;
    public List<ActivoReubicacionDto> activos;
}
