package com.example.diverscan.activeid.data.local.entity;

public class RolEntity {
    private String idRol;
    private String page;
    private String description;
    private String username;
    private String userSysId;
    private boolean estaBloqueado;

    public RolEntity(String idRol, String page, String description, String username, String userSysId, boolean estaBloqueado) {
        this.idRol = idRol;
        this.page = page;
        this.description = description;
        this.username = username;
        this.userSysId = userSysId;
        this.estaBloqueado = estaBloqueado;
    }

    public String getIdRol() { return idRol; }
    public String getPage() { return page; }
    public String getDescription() { return description; }
    public String getUsername() { return username; }
    public String getUserSysId() { return userSysId; }
    public boolean isEstaBloqueado() { return estaBloqueado; }
}
