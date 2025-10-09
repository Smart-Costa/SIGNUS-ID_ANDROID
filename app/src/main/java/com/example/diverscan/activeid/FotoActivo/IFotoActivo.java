package com.example.diverscan.activeid.FotoActivo;

import java.util.ArrayList;

public interface IFotoActivo {
    boolean InsertarFotoDB(String fotoID, String rutaFoto, String nombreFoto ,String fotoConsecutivo, String assetSysId);
    boolean TieneFoto(String assetSysId);
    int CantidadFotos(String AssetSysId);
    ArrayList<EFotoActivo> ObtenerFotoActivo(String AssetSysID);
}
