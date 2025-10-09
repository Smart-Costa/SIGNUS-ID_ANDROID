package com.example.diverscan.activeid.Activo;

public class EntidadActivos {

    String IdActivo;
    String Alias;
    String Descripcion;
    String Departamento;
    String Oficina;
    String Piso;
    String Edificio;
    String Compania;
    String Tag;
    String Numero;
    String CodeBar;
    String IdOficina;
    String IdEstante;
    String IdCategoria;
    String IdPiso;
    String IdEdificio;
    String IdCompania;
    String Marca;
    String Modelo;
    String Serial;
    String ParentAssetSysId;
    String EmployeeRelatedSysId;
    String AssetStatusSysId;
    String AnoFabricacion;
    String Capacidad;
    String EstadoDescripcion;
    String EstadoConservacion;


    public EntidadActivos(String idActivo, String alias, String descripcion, String departamento,
                          String oficina, String piso, String edificio, String compania,
                          String tag, String numero, String codeBar, String idOficina,
                          String idEstante, String idCategoria, String idPiso, String idEdificio,
                          String idCompania, String marca, String modelo, String serial, String parentAssetSysId,
                          String employeeRelatedSysId, String assetStatusSysId, String anoFabricacion,
                          String capacidad, String estadoDescripcion, String estadoConservacion){

        this.IdActivo = idActivo;
        this.Alias = alias;
        this.Descripcion = descripcion;
        this.Departamento = departamento;
        this.Oficina = oficina;
        this.Piso = piso;
        this.Edificio = edificio;
        this.Compania = compania;
        this.Tag = tag;
        this.Numero = numero;
        this.CodeBar = codeBar;
        this.IdOficina = idOficina;
        this.IdEstante = idEstante;
        this.IdCategoria = idCategoria;
        this.IdPiso = idPiso;
        this.IdEdificio = idEdificio;
        this.IdCompania = idCompania;
        this.Marca = marca;
        this.Modelo = modelo;
        this.Serial = serial;
        this.ParentAssetSysId = parentAssetSysId;
        this.EmployeeRelatedSysId = employeeRelatedSysId;
        this.AssetStatusSysId=assetStatusSysId;
        this.AnoFabricacion = anoFabricacion;
        this.Capacidad = capacidad;
        this.EstadoDescripcion = estadoDescripcion;
        this.EstadoConservacion = estadoConservacion;
    }

    public EntidadActivos(String idActivo, String descripcion,
                          String compania,String idCompania,String edificio,String idEdificio, String piso,String idPiso,
                          String oficina,String idOficina,String tag, String numero, String codeBar,
                          String marca, String modelo, String serial,String alias, String idCategoria,
                          String employeeRelatedSysId, String assetStatusSysId, String parentAssetSysId,
                          String anoFabricacion, String capacidad, String estadoDescripcion, String estadoConservacion){

        this.IdActivo = idActivo;
        this.Descripcion = descripcion;
        this.Oficina = oficina;
        this.Piso = piso;
        this.Edificio = edificio;
        this.Compania = compania;
        this.Tag = tag;
        this.Numero = numero;
        this.CodeBar = codeBar;
        this.IdOficina = idOficina;
        this.IdPiso = idPiso;
        this.IdEdificio = idEdificio;
        this.IdCompania = idCompania;
        this.Marca = marca;
        this.Modelo = modelo;
        this.Serial = serial;
        this.Alias = alias;
        this.IdCategoria = idCategoria;
        this.ParentAssetSysId = parentAssetSysId;
        this.EmployeeRelatedSysId = employeeRelatedSysId;
        this.AssetStatusSysId=assetStatusSysId;
        this.AnoFabricacion = anoFabricacion;
        this.Capacidad = capacidad;
        this.EstadoDescripcion = estadoDescripcion;
        this.EstadoConservacion = estadoConservacion;
    }


    public String getIdActivo (){
        return IdActivo;
    }

    public String getAlias (){
        return Alias;
    }

    public String getDescripcion (){
        return Descripcion;
    }

    public String getDepartamento (){
        return Departamento;
    }

    public String getOficina (){
        return Oficina;
    }

    public String getPiso (){
        return Piso;
    }

    public String getEdificio (){
        return Edificio;
    }

    public String getCompania (){
        return Compania;
    }

    public String getTag (){
        return Tag;
    }

    public String getNumero (){
        return Numero;
    }

    public String getCodeBar (){
        return CodeBar;
    }

    public String getIdOficina (){
        return IdOficina;
    }

    public String getIdEstante (){
        return IdEstante;
    }

    public String getIdCategoria (){
        return IdCategoria;
    }

    public String getIdPiso (){
        return IdPiso;
    }

    public String getIdEdificio (){
        return IdEdificio;
    }

    public String getIdCompania (){
        return IdCompania;
    }

    public String getMarca (){
        return Marca;
    }

    public String getModelo (){
        return Modelo;
    }

    public String getSerial (){
        return Serial;
    }

    public void setIdActivo (String  idActivo){
        IdActivo = idActivo;
    }

    public void setAlias (String  alias){
        Alias = alias;
    }

    public void setDescripcion (String  descripcion){
        Descripcion = descripcion;
    }

    public void setDepartamento (String  departamento){
        Departamento = departamento;
    }

    public void setOficina (String  oficina){
        Oficina = oficina;
    }

    public void setPiso (String  piso){
        Piso = piso;
    }

    public void setEdificio (String  edificio){
        Edificio = edificio;
    }

    public void setCompania (String  compania){
        Compania = compania;
    }

    public void setTag (String  tag){
        Tag = tag;
    }

    public void setNumero (String  numero){
        Numero = numero;
    }

    public void setCodeBar (String  codeBar){
        CodeBar = codeBar;
    }

    public void setIdOficina (String  idOficina){
        IdOficina = idOficina;
    }

    public void setIdEstante (String  idEstante){
        IdEstante = idEstante;
    }

    public void setIdCategoria (String  idCategoria){
        IdCategoria = idCategoria;
    }

    public void setIdPiso (String  idPiso){
        IdPiso = idPiso;
    }

    public void setIdEdificio (String  idEdificio){
        IdEdificio = idEdificio;
    }

    public void setIdCompania (String  idCompania){
        IdCompania = idCompania;
    }

    public void setMarca (String  marca){
        Marca = marca;
    }

    public void setModelo (String  modelo){
        Modelo = modelo;
    }

    public void setSerial (String  serial){
        Serial = serial;
    }

    public String getParentAssetSysId() {
        return ParentAssetSysId;
    }

    public void setParentAssetSysId(String parentAssetSysId) {
        ParentAssetSysId = parentAssetSysId;
    }

    public String getEmployeeRelatedSysId() {
        return EmployeeRelatedSysId;
    }

    public void setEmployeeRelatedSysId(String pEmployeeRelatedSysId) {
        EmployeeRelatedSysId = pEmployeeRelatedSysId;
    }

    public String getAssetStatusSysId() {
        return AssetStatusSysId;
    }

    public void setAssetStatusSysId(String pAssetStatusSysId) {
        AssetStatusSysId = pAssetStatusSysId;
    }

    public String getEstadoDescripcion() {
        return EstadoDescripcion;
    }

    public void setEstadoDescripcion(String estadoDescripcion) {
        EstadoDescripcion = estadoDescripcion;
    }

    public String getEstadoConservacion() {
        return EstadoConservacion;
    }

    public void setEstadoConservacion(String estadoConservacion) {
        EstadoConservacion = estadoConservacion;
    }
    public String getAnoFabricacion() {return AnoFabricacion;}

    public void setAnoFabricacion(String anofabricacion){
        AnoFabricacion = anofabricacion;
    }

    public String getCapacidad() { return Capacidad; }

    public void setCapacidad (String capacidad) {Capacidad = capacidad;}

    @Override
    public String toString(){
        return this.Alias;
    }

}


