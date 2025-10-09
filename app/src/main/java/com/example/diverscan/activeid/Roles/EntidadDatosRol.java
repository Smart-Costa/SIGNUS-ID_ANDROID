package com.example.diverscan.activeid.Roles;

public class EntidadDatosRol {

    private String _idRol;
    private String description;
    private String page;
    private String username;
    private String userSysId;
    private String estaBloqueado;

    public EntidadDatosRol(String IdRol, String Description,String Page, String Username, String UserSysId, String EstaBloqueado){

        _idRol = IdRol;
        description = Description;
        page= Page;
        username = Username;
        userSysId = UserSysId;
        estaBloqueado = EstaBloqueado;
    }

    public String get_idRol() {
        return _idRol;
    }

    public void set_idRol(String _idRol) {
        this._idRol = _idRol;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String code) {
        this.page = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String code) {
        this.description= code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String code) {
        this.username = code;
    }

    public String getUserSysId() {
        return userSysId;
    }
    public void setUserSysId(String userSysId) {
        this.userSysId = userSysId;
    }
    public String getEstaBloqueado() {
        return estaBloqueado;
    }
    public void setEstaBloqueado(String estaBloqueado) {
        this.estaBloqueado = estaBloqueado;
    }
}
