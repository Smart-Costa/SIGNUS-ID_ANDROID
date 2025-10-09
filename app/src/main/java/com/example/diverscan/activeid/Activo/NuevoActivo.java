package com.example.diverscan.activeid.Activo;

public class NuevoActivo {


    String assetID;
    String Numero;
    String CodeBar;
    String Descripcion;
    String idCompania;
    String idEdificio;
    String idPiso;
    String idOficina;
    String EmployeeRelated;
    String Marca;
    String Modelo;
    String Serial;
    String Tag;
    String Compania;
    String Edificio;
    String Piso;
    String Oficina;
    String parentAssetSysId;
    String AssetStatusSysId;
    String entryUser;
    String AnoFabricacion;
    String Capacidad;
    String EstadoDescripcion;
    String EstadoConservacion;
    public void NuevoActivo(String assetId, String codeBar,  String numero,  String descripcion, String idcompania,
                       String idedificio,String idpiso,String idoficina, String employeeRelated,
                       String marca, String modelo, String serial, String tag, String compania, String edificio, String piso, String oficina, String assetStatusSysId){

        this.assetID= assetId;
        this.CodeBar = codeBar;
        this.Numero = numero;
        this.Descripcion = descripcion;
        this.idCompania = idcompania;
        this.idEdificio = idedificio;
        this.idPiso = idpiso;
        this.idOficina = idoficina;
        this.EmployeeRelated = employeeRelated;
        this.Marca = marca;
        this.Modelo = modelo;
        this.Serial = serial;
        this.Tag = tag;
        this.Compania = compania;
        this.Edificio = edificio;
        this.Piso = piso;
        this.Oficina = oficina;
        this.AssetStatusSysId = assetStatusSysId;
    }

    public NuevoActivo(String assetId, String numero, String codeBar, String descripcion,String idcompania,
                          String idedificio, String idPiso, String idoficina, String employeeRelated,
                          String marca, String modelo, String serial,String tag, String compania,
                       String edificio, String piso, String oficina, String ParentSysId, String assetStatusSysId, String EntryUser,
                       String AnoFabricacion, String Capacidad, String EstadoDescripcion, String EstadoConservacion){

        this.assetID = assetId;
        this.Descripcion = descripcion;
        this.idCompania = idcompania;
        this.idEdificio = idedificio;
        this.idPiso = idPiso;
        this.idOficina = idoficina;
        this.Tag = tag;
        this.Numero = numero;
        this.CodeBar = codeBar;
        this.Marca = marca;
        this.Modelo = modelo;
        this.Serial = serial;
        this.EmployeeRelated = employeeRelated;
        this.Compania = compania;
        this.Edificio = edificio;
        this.Piso = piso;
        this.Oficina = oficina;
        this.parentAssetSysId = ParentSysId;
        this.AssetStatusSysId = assetStatusSysId;
        this.entryUser = EntryUser;
        this.AnoFabricacion = AnoFabricacion;
        this.Capacidad = Capacidad;
        this.EstadoDescripcion = EstadoDescripcion;
        this.EstadoConservacion = EstadoConservacion;
    }

    public String getAnoFabricacion() {
        return AnoFabricacion;
    }

    public void setAnoFabricacion(String anoFabricacion) {
        AnoFabricacion = anoFabricacion;
    }

    public String getCapacidad() {
        return Capacidad;
    }

    public void setCapacidad(String capacidad) {
        Capacidad = capacidad;
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
    public String getParentAssetSysId() {
        return parentAssetSysId;
    }

    public void setParentAssetSysId(String parentAssetSysId) {
        this.parentAssetSysId = parentAssetSysId;
    }

    public String getAssetId(){return assetID;}

    public String getAssetStatusSysId() {
        return AssetStatusSysId;
    }

    public String getEntryUser() {
        return entryUser;
    }

    public void setEntryUser(String entryUser) {
        this.entryUser = entryUser;
    }

    public void setAssetStatusSysId(String assetStatusSysId) {
        AssetStatusSysId = assetStatusSysId;
    }

    public String getIdEdificio() {
        return idEdificio;
    }

    public String getIdCompania(){
        return idCompania;
    }

    public String getIdPiso(){
        return idPiso;
    }

    public String getIdOficina(){
        return idOficina;
    }

    public String getTag (){
        return Tag;
    }

    public String getNumero (){
        return Numero;
    }

    public String getEmployeeRelated (){
        return EmployeeRelated;
    }

    public String getCodeBar (){
        return CodeBar;
    }

    public String getDescripcion (){
        return Descripcion;
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

    public String getCompania() {return  Compania;}
    public String getEdificio() {return  Edificio;}
    public String getPiso() {return  Piso;}
    public String getOficina() {return  Oficina;}


    public void setAssetID(String assetId) {
        assetID = assetId;
    }



    public void setDescripcion (String  descripcion){
        Descripcion = descripcion;
    }

    public void setEmployeeRelated (String  departamento){
        EmployeeRelated = departamento;
    }

    public void setIdPiso(String idpiso){
        idPiso = idpiso;
    }




    public void setIdEdificio (String idedificio){
        idEdificio = idedificio;
    }



    public void setIdCompania (String idcompania){
        idCompania= idcompania;
    }


    public void setIdOficina (String idoficina){
        idOficina = idoficina;
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

    public void setMarca (String  marca){
        Marca = marca;
    }

    public void setModelo (String  modelo){
        Modelo = modelo;
    }

    public void setCompania (String  compania){
        Compania = compania;
    }

    public void setEdificio (String  edificio){
        Edificio = edificio;
    }

    public void setPiso (String  piso){
        Piso = piso;
    }

    public void setOficina (String  oficina){
        Oficina = oficina;
    }

    @Override
    public String toString(){
        return this.assetID;
    }

}
