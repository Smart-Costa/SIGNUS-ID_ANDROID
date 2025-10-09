package com.example.diverscan.activeid.Activo;

import java.io.Serializable;

public class EntidadSubActivo implements Serializable {
    String IdActivo;
    String ParentId;
    String Alias;
    String Descripcion;
    String Tag;
    String Numero;
    String CodeBar;

    public EntidadSubActivo() {
    }

    public EntidadSubActivo(String idActivo, String parentId, String alias, String descripcion, String tag, String numero, String codeBar) {
        IdActivo = idActivo;
        ParentId = parentId;
        Alias = alias;
        Descripcion = descripcion;
        Tag = tag;
        Numero = numero;
        CodeBar = codeBar;
    }

    public String getIdActivo() {
        return IdActivo;
    }

    public void setIdActivo(String idActivo) {
        IdActivo = idActivo;
    }

    public String getPaternId() {
        return ParentId;
    }

    public void setPaternId(String paternId) {
        ParentId = paternId;
    }

    public String getAlias() {
        return Alias;
    }

    public void setAlias(String alias) {
        Alias = alias;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        Tag = tag;
    }

    public String getNumero() {
        return Numero;
    }

    public void setNumero(String numero) {
        Numero = numero;
    }

    public String getCodeBar() {
        return CodeBar;
    }

    public void setCodeBar(String codeBar) {
        CodeBar = codeBar;
    }
}
