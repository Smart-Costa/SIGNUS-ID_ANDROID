package com.example.diverscan.activeid.data.local.entity;

public class RolEntity {
    private String IdRol;
    private String Page;
    private String Description;
    private String Username;
    private String UserSysId;
    private boolean Esta_Bloqueado;

    public RolEntity(String IdRol, String Page, String Description, String Username, String UserSysId, boolean Esta_Bloqueado) {
        this.IdRol = IdRol;
        this.Page = Page;
        this.Description = Description;
        this.Username = Username;
        this.UserSysId = UserSysId;
        this.Esta_Bloqueado = Esta_Bloqueado;
    }

    public String getIdRol() { return IdRol; }
    public String getPage() { return Page; }
    public String getDescription() { return Description; }
    public String getUsername() { return Username; }
    public String getUserSysId() { return UserSysId; }
    public boolean isEstaBloqueado() { return Esta_Bloqueado; }
}
