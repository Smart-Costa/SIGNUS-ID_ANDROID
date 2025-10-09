package com.example.diverscan.activeid.Activo;

public class ActivoRecord
{

    private String _Alias;
    private String _Descripcion;
    private String _Tag;
    private String _Serial;
    //private String _Numero;
    private String _IdOficina;
    private String _IdPiso;
    private String _IdEdificio;
    private String _IdCompania;
    private String _Marca;
    private String _Modelo;
    private String _IdActivo;
    private String _CodeBar;
    private String _UpdateUser;
    private String _ParentAssetSysId;
    private String _EmployeeRelatedSysId;
    private String _AssetStatusSysId;
    private String _AnoFabricacion;
    private String _Capacidad;
    private String _EstadoDescripcion;
    private String _EstadoConservacion;

    public ActivoRecord(String Alias, String Descripcion,String Tag, String idActivo, String IdOficina,
                        String IdPiso, String IdEdificio, String IdCompania, String Marca, String Modelo,
                        String Serial, String CodeBar,String UpdateUser, String ParentAssetSysId, String EmployeeRelatedSysId,
                        String AssetStatusSysId, String anoFabricacion, String capacidad, String estadoDescripcion,
                        String estadoConservacion){

        _Alias = Alias;
        _Descripcion = Descripcion;
        _Tag = Tag;
        _IdActivo = idActivo;
        _IdOficina = IdOficina;
        _IdPiso = IdPiso;
        _IdEdificio = IdEdificio;
        _IdCompania = IdCompania;
        _Marca = Marca;
        _Modelo = Modelo;
        _Serial = Serial;
        _CodeBar = CodeBar;
        _UpdateUser = UpdateUser;
        _ParentAssetSysId = ParentAssetSysId;
        _EmployeeRelatedSysId = EmployeeRelatedSysId;
        _AssetStatusSysId = AssetStatusSysId;
        _AnoFabricacion = anoFabricacion;
        _Capacidad = capacidad;
        _EstadoDescripcion=estadoDescripcion;
        _EstadoConservacion=estadoConservacion;
    }
    public ActivoRecord(String Alias, String Descripcion,String Tag, String idActivo, String IdOficina,
                        String IdPiso, String IdEdificio, String IdCompania, String Marca, String Modelo,
                        String Serial, String CodeBar,String UpdateUser, String AnoFabricacion, String Capacidad)
    {

        _Alias = Alias;
        _Descripcion = Descripcion;
        _Tag = Tag;
        _IdActivo = idActivo;
        _IdOficina = IdOficina;
        _IdPiso = IdPiso;
        _IdEdificio = IdEdificio;
        _IdCompania = IdCompania;
        _Marca = Marca;
        _Modelo = Modelo;
        _Serial = Serial;
        _CodeBar = CodeBar;
        _UpdateUser = UpdateUser;
        _AnoFabricacion = AnoFabricacion;
        _Capacidad = Capacidad;
    }

    public String get_ParentAssetSysId() {
        return _ParentAssetSysId;
    }

    public void set_ParentAssetSysId(String _ParentAssetSysId) {
        this._ParentAssetSysId = _ParentAssetSysId;
    }

    public String get_UpdateUser() {
        return _UpdateUser;
    }

    public void set_UpdateUser(String _UpdateUser) {
        this._UpdateUser = _UpdateUser;
    }

    public String getAlias (){
        return _Alias;
    }

    public String getDescripcion (){
        return _Descripcion;
    }

    public String getTag (){
        return _Tag;
    }

    public String getSerial (){
        return _Serial;
    }
    public String getIdActivo (){ return _IdActivo;}

    public String getIdOficina (){
        return _IdOficina;
    }

    public String getIdPiso (){
        return _IdPiso;
    }

    public String getIdEdificio (){
        return _IdEdificio;
    }

    public String getIdCompania (){
        return _IdCompania;
    }

    public String getMarca (){
        return _Marca;
    }

    public String getModelo (){
        return _Modelo;
    }

    public String getCodeBar (){
        return _CodeBar;
    }


    public void setAlias (String  alias){
        _Alias = alias;
    }

    public void setDescripcion (String  descripcion){
        _Descripcion = descripcion;
    }

    public void setTag (String  idtag){
        _Tag = idtag;
    }

    public void setNumero (String  serial){
        _Serial = serial;
    }
    public void setIdActivo (String idActivo){
        _IdActivo= idActivo;
    }

    public void setIdOficina (String  idOficina){
        _IdOficina = idOficina;
    }

    public void setIdPiso (String  idPiso){
        _IdPiso = idPiso;
    }

    public void setIdEdificio (String  idEdificio){
        _IdEdificio = idEdificio;
    }

    public void setIdCompania (String  idCompania){
        _IdCompania = idCompania;
    }

    public void setMarca (String  marca){
        _Marca = marca;
    }

    public void setModelo (String  modelo){
        _Modelo = modelo;
    }

    public void set_CodeBar (String CodeBar) {
        _CodeBar = CodeBar;
    }

    public String getEmployeeRelatedSysId() {
        return _EmployeeRelatedSysId;
    }

    public void setEmployeeRelatedSysId(String pEmployeeRelatedSysId) {
        _EmployeeRelatedSysId = pEmployeeRelatedSysId;
    }

    public String getAssetStatusSysId() {
        return _AssetStatusSysId;
    }

    public void setAssetStatusSysId(String pAssetStatusSysId) {
        _AssetStatusSysId = pAssetStatusSysId;
    }
    public String get_EstadoDescripcion() {
        return _EstadoDescripcion;
    }

    public void set_EstadoDescripcion(String _EstadoDescripcion) {
        this._EstadoDescripcion = _EstadoDescripcion;
    }

    public String get_EstadoConservacion() {
        return _EstadoConservacion;
    }

    public void set_EstadoConservacion(String _EstadoConservacion) {
        this._EstadoConservacion = _EstadoConservacion;
    }
    public String getAnoFabricacion() {return _AnoFabricacion;}

    public void setAnoFabricacion(String pAnoFabricacion){
        _AnoFabricacion = pAnoFabricacion;
    }

    public String getCapacidad() { return _Capacidad; }

    public void setCapacidad (String pCapacidad) {_Capacidad = pCapacidad;}


    @Override
    public String toString(){
        return this._Alias;
    }



}
