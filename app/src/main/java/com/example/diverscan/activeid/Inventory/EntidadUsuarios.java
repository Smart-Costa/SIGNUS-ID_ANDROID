package com.example.diverscan.activeid.Inventory;

public class EntidadUsuarios {

    String UserSysId;
    String UserName;
    String Email;
    String Password;
    String bloqueado;
    String aprobado;
    String sesionActiva;
    String contrasenaFallida;
    String UltimaActividad;
    String UltimoInicio;
    String FechaBloqueo;

    public EntidadUsuarios(String id , String userName , String email , String password, String bloqueado,
                           String aprobado, String sesionActiva, String contrasenaFallida, String UltimaActividad,
                           String UltimoInicio, String FechaBloqueo)   {

        this.UserSysId = id ;
        this.UserName = userName ;
        this.Email = email ;
        this.Password = password ;
        this.bloqueado = bloqueado;
        this.aprobado = aprobado;
        this.sesionActiva = sesionActiva;
        this.contrasenaFallida = contrasenaFallida;
        this.UltimaActividad = UltimaActividad;
        this.UltimoInicio = UltimoInicio;
        this.FechaBloqueo = FechaBloqueo;
    }

    public String getUserSysId() {
        return UserSysId;
    }

    public void setUserSysId(String userSysId) {
        UserSysId = userSysId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }


    public String getBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(String bloqueado) {
        this.bloqueado = bloqueado;
    }

    public String getAprobado() {
        return aprobado;
    }

    public void setAprobado(String aprobado) {
        this.aprobado = aprobado;
    }

    public String getSesionActiva() {
        return sesionActiva;
    }

    public void setSesionActiva(String sesionActiva) {
        this.sesionActiva = sesionActiva;
    }

    public String getContrasenaFallida() {
        return contrasenaFallida;
    }

    public void setContrasenaFallida(String contrasenaFallida) {
        this.contrasenaFallida = contrasenaFallida;
    }

    public String getUltimaActividad() {
        return UltimaActividad;
    }

    public void setUltimaActividad(String ultimaActividad) {
        UltimaActividad = ultimaActividad;
    }

    public String getUltimoInicio() {
        return UltimoInicio;
    }

    public void setUltimoInicio(String ultimoInicio) {
        UltimoInicio = ultimoInicio;
    }

    public String getFechaBloqueo() {
        return FechaBloqueo;
    }

    public void setFechaBloqueo(String fechaBloqueo) {
        FechaBloqueo = fechaBloqueo;
    }


    @Override
    public String toString(){
        return this.UserName;
    }

}

