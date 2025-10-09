package com.example.diverscan.activeid;

public class User {

    public   String   id ;
    public   String   userName ;
    public   String   email ;
    public   String   password ;
    public  String bloqueado;
    public String aprobado;
    public String sesionActiva;
    public String contrasenaFallida;
    public String UltimaActividad;
    public String UltimoInicio;
    public String FechaBloqueo;



    public User(String id ,String userName ,String email ,String password, String bloqueado,
                String aprobado, String sesionActiva, String contrasenaFallida, String UltimaActividad,
                String UltimoInicio, String FechaBloqueo)   {

        this.id = id ;
        this.userName = userName ;
        this.email = email ;
        this.password = password ;
        this.bloqueado = bloqueado;
        this.aprobado = aprobado;
        this.sesionActiva = sesionActiva;
        this.contrasenaFallida = contrasenaFallida;
        this.UltimaActividad = UltimaActividad;
        this.UltimoInicio = UltimoInicio;
        this.FechaBloqueo = FechaBloqueo;
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


}
