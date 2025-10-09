package com.example.diverscan.activeid.Inventory;

public class EntidadRazonSocial {

    String IdRazon;
    String NombreRazon;

   public EntidadRazonSocial (String idrazon, String nombrerazon){

       this.IdRazon = idrazon;
       this.NombreRazon = nombrerazon;
   }

   public String getIdRazon(){
       return IdRazon;
   }

   public String getNombreRazon(){
       return NombreRazon;
   }

   public void setIdRazon(String idRazon){
       IdRazon = idRazon;
   }

   public void setNombreRazon (String nombreRazon){
       NombreRazon = nombreRazon;
   }

   @Override
    public String toString(){
       return this.NombreRazon;
   }
}
