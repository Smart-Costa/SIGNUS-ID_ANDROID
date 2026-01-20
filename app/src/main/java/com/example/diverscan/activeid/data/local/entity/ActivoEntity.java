package com.example.diverscan.activeid.data.local.entity;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.UUID;

public class ActivoEntity {

    @SerializedName(value = "ID_ACTIVO", alternate = {"IdActivo", "idActivo"})
    @NonNull
    private String idActivo = UUID.randomUUID().toString();

    @SerializedName(value = "NUMERO_ACTIVO", alternate = {"NumeroActivo", "numeroActivo", "Placa", "placa"})
    private String numeroActivo;

    @SerializedName(value = "NUMERO_ETIQUETA", alternate = {"NumeroEtiqueta", "numeroEtiqueta", "Etiqueta", "etiqueta", "NoActivo", "noActivo"})
    private String numeroEtiqueta;

    @SerializedName(value = "DESCRIPCION_CORTA", alternate = {"DescripcionCorta", "descripcionCorta", "Nombre", "nombre", "Descripcion", "descripcion"})
    private String descripcionCorta;

    @SerializedName(value = "DESCRIPCION_LARGA", alternate = {"DescripcionLarga", "descripcionLarga"})
    private String descripcionLarga;

    @SerializedName(value = "CATEGORIA", alternate = {"Categoria", "categoria"})
    private String categoria;

    @SerializedName(value = "ESTADO", alternate = {"Estado", "estado"})
    private String estado;

    @SerializedName(value = "EMPRESA", alternate = {"Empresa", "empresa"})
    private String empresa;

    @SerializedName(value = "MARCA", alternate = {"Marca", "marca"})
    private String marca;

    @SerializedName(value = "MODELO", alternate = {"Modelo", "modelo"})
    private String modelo;

    @SerializedName(value = "NUMERO_SERIE", alternate = {"NumeroSerie", "numeroSerie", "Serie", "serie"})
    private String numeroSerie;

    @SerializedName(value = "COSTO", alternate = {"Costo", "costo"})
    private Double costo;

    @SerializedName(value = "NUMERO_FACTURA", alternate = {"NumeroFactura", "numeroFactura", "Factura", "factura"})
    private String numeroFactura;

    @SerializedName(value = "FECHA_COMPRA", alternate = {"FechaCompra", "fechaCompra"})
    private String fechaCompra;

    @SerializedName(value = "FECHA_CAPITALIZACION", alternate = {"FechaCapitalizacion", "fechaCapitalizacion"})
    private String fechaCapitalizacion;

    @SerializedName(value = "VALOR_RESIDUAL", alternate = {"ValorResidual", "valorResidual"})
    private Double valorResidual;

    @SerializedName(value = "DOCUMENTO", alternate = {"Documento", "documento"})
    private String documento;

    @SerializedName(value = "FOTOS", alternate = {"Fotos", "fotos", "Foto", "foto"})
    private String fotos;

    @SerializedName(value = "NUMERO_PARTE_FABRICANTE", alternate = {"NumeroParteFabricante", "numeroParteFabricante"})
    private String numeroParteFabricante;

    @SerializedName(value = "DEPRECIADO", alternate = {"Depreciado", "depreciado"})
    private String depreciado;

    @SerializedName(value = "DESCRIPCION_DEPRECIADO", alternate = {"DescripcionDepreciado", "descripcionDepreciado"})
    private String descripcionDepreciado;

    @SerializedName(value = "ANOS_VIDA_UTIL", alternate = {"AnosVidaUtil", "anosVidaUtil"})
    private Integer anosVidaUtil;

    @SerializedName(value = "CUENTA_CONTABLE_DEPRESIACION", alternate = {"CuentaContableDepresiacion", "cuentaContableDepresiacion"})
    private String cuentaContableDepresiacion;

    @SerializedName(value = "CENTRO_COSTOS", alternate = {"CentroCostos", "centroCostos"})
    private String centroCostos;

    @SerializedName(value = "DESCRIPCION_ESTADO_ULTIMO_INVENTARIO", alternate = {"DescripcionEstadoUltimoInventario", "descripcionEstadoUltimoInventario"})
    private String descripcionEstadoUltimoInventario;

    @SerializedName(value = "TAG_EPC", alternate = {"TagEpc", "tagEpc"})
    private String tagEpc;

    @SerializedName(value = "EMPLEADO", alternate = {"Empleado", "empleado"})
    private String empleado;

    @SerializedName(value = "UBICACION_A", alternate = {"UbicacionA", "ubicacionA"})
    private String ubicacionA;

    @SerializedName(value = "UBICACION_B", alternate = {"UbicacionB", "ubicacionB"})
    private String ubicacionB;

    @SerializedName(value = "UBICACION_C", alternate = {"UbicacionC", "ubicacionC"})
    private String ubicacionC;

    @SerializedName(value = "UBICACION_D", alternate = {"UbicacionD", "ubicacionD"})
    private String ubicacionD;

    @SerializedName(value = "UBICACION_SECUNDARIA", alternate = {"UbicacionSecundaria", "ubicacionSecundaria"})
    private String ubicacionSecundaria;

    @SerializedName(value = "FECHA_GARANTIA", alternate = {"FechaGarantia", "fechaGarantia"})
    private String fechaGarantia;

    @SerializedName(value = "COLOR", alternate = {"Color", "color"})
    private String color;

    @SerializedName(value = "TAMANIO_MEDIDA", alternate = {"TamanioMedida", "tamanioMedida"})
    private String tamanioMedida;

    @SerializedName(value = "OBSERVACIONES", alternate = {"Observaciones", "observaciones"})
    private String observaciones;

    @SerializedName(value = "ESTADO_ACTIVO", alternate = {"EstadoActivo", "estadoActivo"})
    private Boolean estadoActivo;

    @SerializedName(value = "FECHA_CREACION_ACTIVO", alternate = {"FechaCreacionActivo", "fechaCreacionActivo"})
    private String fechaCreacionActivo;

    @SerializedName(value = "EPC", alternate = {"Epc", "epc"})
    private String epc;

    @SerializedName(value = "CATEGORIA_A", alternate = {"CategoriaA", "categoriaA"})
    private String categoriaA;

    @SerializedName(value = "CATEGORIA_B", alternate = {"CategoriaB", "categoriaB"})
    private String categoriaB;

    @SerializedName(value = "CATEGORIA_C", alternate = {"CategoriaC", "categoriaC"})
    private String categoriaC;

    @SerializedName(value = "UBICACION_LOGICA_A", alternate = {"UbicacionLogicaA", "ubicacionLogicaA"})
    private String ubicacionLogicaA;

    @SerializedName(value = "UBICACION_LOGICA_B", alternate = {"UbicacionLogicaB", "ubicacionLogicaB"})
    private String ubicacionLogicaB;

    @SerializedName(value = "UBICACION_LOGICA_C", alternate = {"UbicacionLogicaC", "ubicacionLogicaC"})
    private String ubicacionLogicaC;

    @SerializedName(value = "ENTIDAD_ASOCIADA", alternate = {"EntidadAsociada", "entidadAsociada"})
    private String entidadAsociada;

    @SerializedName(value = "COSTO_DEPRECIACION", alternate = {"CostoDepreciacion", "costoDepreciacion"})
    private Double costoDepreciacion;

    @SerializedName(value = "UNIDAD_ORGANIZATIVA", alternate = {"UnidadOrganizativa", "unidadOrganizativa"})
    private String unidadOrganizativa;

    // --- Getters y Setters ---

    @NonNull
    public String getIdActivo() { return idActivo; }
    public void setIdActivo(@NonNull String idActivo) { this.idActivo = idActivo; }

    public String getNumeroActivo() {
        return numeroActivo;
    }
    public void setNumeroActivo(String numeroActivo) { this.numeroActivo = numeroActivo; }

    public String getNumeroEtiqueta() { return numeroEtiqueta; }
    public void setNumeroEtiqueta(String numeroEtiqueta) { this.numeroEtiqueta = numeroEtiqueta; }

    public String getDescripcionCorta() { return descripcionCorta; }
    public void setDescripcionCorta(String descripcionCorta) { this.descripcionCorta = descripcionCorta; }

    public String getDescripcionLarga() { return descripcionLarga; }
    public void setDescripcionLarga(String descripcionLarga) { this.descripcionLarga = descripcionLarga; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public Double getCosto() { return costo; }
    public void setCosto(Double costo) { this.costo = costo; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(String fechaCompra) { this.fechaCompra = fechaCompra; }

    public String getFechaCapitalizacion() { return fechaCapitalizacion; }
    public void setFechaCapitalizacion(String fechaCapitalizacion) { this.fechaCapitalizacion = fechaCapitalizacion; }

    public Double getValorResidual() { return valorResidual; }
    public void setValorResidual(Double valorResidual) { this.valorResidual = valorResidual; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getFotos() { return fotos; }
    public void setFotos(String fotos) { this.fotos = fotos; }

    public String getNumeroParteFabricante() { return numeroParteFabricante; }
    public void setNumeroParteFabricante(String numeroParteFabricante) { this.numeroParteFabricante = numeroParteFabricante; }

    public String getDepreciado() { return depreciado; }
    public void setDepreciado(String depreciado) { this.depreciado = depreciado; }

    public String getDescripcionDepreciado() { return descripcionDepreciado; }
    public void setDescripcionDepreciado(String descripcionDepreciado) { this.descripcionDepreciado = descripcionDepreciado; }

    public Integer getAnosVidaUtil() { return anosVidaUtil; }
    public void setAnosVidaUtil(Integer anosVidaUtil) { this.anosVidaUtil = anosVidaUtil; }

    public String getCuentaContableDepresiacion() { return cuentaContableDepresiacion; }
    public void setCuentaContableDepresiacion(String cuentaContableDepresiacion) { this.cuentaContableDepresiacion = cuentaContableDepresiacion; }

    public String getCentroCostos() { return centroCostos; }
    public void setCentroCostos(String centroCostos) { this.centroCostos = centroCostos; }

    public String getDescripcionEstadoUltimoInventario() { return descripcionEstadoUltimoInventario; }
    public void setDescripcionEstadoUltimoInventario(String descripcionEstadoUltimoInventario) { this.descripcionEstadoUltimoInventario = descripcionEstadoUltimoInventario; }

    public String getTagEpc() { return tagEpc; }
    public void setTagEpc(String tagEpc) { this.tagEpc = tagEpc; }

    public String getEmpleado() { return empleado; }
    public void setEmpleado(String empleado) { this.empleado = empleado; }

    public String getUbicacionA() { return ubicacionA; }
    public void setUbicacionA(String ubicacionA) { this.ubicacionA = ubicacionA; }

    public String getUbicacionB() { return ubicacionB; }
    public void setUbicacionB(String ubicacionB) { this.ubicacionB = ubicacionB; }

    public String getUbicacionC() { return ubicacionC; }
    public void setUbicacionC(String ubicacionC) { this.ubicacionC = ubicacionC; }

    public String getUbicacionD() { return ubicacionD; }
    public void setUbicacionD(String ubicacionD) { this.ubicacionD = ubicacionD; }

    public String getUbicacionSecundaria() { return ubicacionSecundaria; }
    public void setUbicacionSecundaria(String ubicacionSecundaria) { this.ubicacionSecundaria = ubicacionSecundaria; }

    public String getFechaGarantia() { return fechaGarantia; }
    public void setFechaGarantia(String fechaGarantia) { this.fechaGarantia = fechaGarantia; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getTamanioMedida() { return tamanioMedida; }
    public void setTamanioMedida(String tamanioMedida) { this.tamanioMedida = tamanioMedida; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    public String getFechaCreacionActivo() { return fechaCreacionActivo; }
    public void setFechaCreacionActivo(String fechaCreacionActivo) { this.fechaCreacionActivo = fechaCreacionActivo; }

    public String getEpc() { return epc; }
    public void setEpc(String epc) { this.epc = epc; }

    public String getCategoriaA() { return categoriaA; }
    public void setCategoriaA(String categoriaA) { this.categoriaA = categoriaA; }

    public String getCategoriaB() { return categoriaB; }
    public void setCategoriaB(String categoriaB) { this.categoriaB = categoriaB; }

    public String getCategoriaC() { return categoriaC; }
    public void setCategoriaC(String categoriaC) { this.categoriaC = categoriaC; }

    public String getUbicacionLogicaA() { return ubicacionLogicaA; }
    public void setUbicacionLogicaA(String ubicacionLogicaA) { this.ubicacionLogicaA = ubicacionLogicaA; }

    public String getUbicacionLogicaB() { return ubicacionLogicaB; }
    public void setUbicacionLogicaB(String ubicacionLogicaB) { this.ubicacionLogicaB = ubicacionLogicaB; }

    public String getUbicacionLogicaC() { return ubicacionLogicaC; }
    public void setUbicacionLogicaC(String ubicacionLogicaC) { this.ubicacionLogicaC = ubicacionLogicaC; }

    public String getEntidadAsociada() { return entidadAsociada; }
    public void setEntidadAsociada(String entidadAsociada) { this.entidadAsociada = entidadAsociada; }

    public Double getCostoDepreciacion() { return costoDepreciacion; }
    public void setCostoDepreciacion(Double costoDepreciacion) { this.costoDepreciacion = costoDepreciacion; }

    public String getUnidadOrganizativa() { return unidadOrganizativa; }
    public void setUnidadOrganizativa(String unidadOrganizativa) { this.unidadOrganizativa = unidadOrganizativa; }
}
