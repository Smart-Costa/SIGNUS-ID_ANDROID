package com.example.diverscan.activeid.Inventory;

import java.io.Serializable;

public class Entidad_TomaFisica implements Serializable {
     String IdToma;
     String TakeDate;
     String TakeDescription;
     String TakeName;
     String TakeStatus;

     public Entidad_TomaFisica(String idToma, String takeDate, String takeDescription, String takeName, String takeStatus){
         this.IdToma = idToma;
         this.TakeDate= takeDate;
         this.TakeDescription = takeDescription;
         this.TakeName = takeName;
         this.TakeStatus = takeStatus;
     }

    public String getIdToma() {
        return IdToma;
    }

    public String getTakeDate() {
        return TakeDate;
    }

    public String getTakeDescription() {
        return TakeDescription;
    }

    public String getTakeName() {
        return TakeName;
    }

    public String getTakeStatus() {
        return TakeStatus;
    }

    public void setIdToma(String idToma) {
        IdToma = idToma;
    }

    public void setTakeDate(String takeDate) {
        TakeDate = takeDate;
    }

    public void setTakeDescription(String takeDescription) {
        TakeDescription = takeDescription;
    }

    public void setTakeName(String takeName) {
        TakeName = takeName;
    }

    public void setTakeStatus(String takeStatus) {
        TakeStatus = takeStatus;
    }
}
