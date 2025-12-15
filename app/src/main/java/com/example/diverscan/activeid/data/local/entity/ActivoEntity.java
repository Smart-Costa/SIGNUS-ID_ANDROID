package com.example.diverscan.activeid.data.local.entity;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class ActivoEntity {

    @SerializedName("ID_ACTIVO")
    @NonNull
    private String idActivo = UUID.randomUUID().toString();

    @SerializedName("NUMERO_ACTIVO")
    private String numeroActivo;

    @SerializedName("NUMERO_ETIQUETA")
    private String numeroEtiqueta;

    @SerializedName("DESCRIPCION_CORTA")
    private String descripcionCorta;

    @SerializedName("DESCRIPCION_LARGA")
    private String descripcionLarga;

    @SerializedName("CATEGORIA")
    private String categoria;

    @SerializedName("ESTADO")
    private String estado;

    @SerializedName("EMPRESA")
    private String empresa;

    @SerializedName("MARCA")
    private String marca;

    @SerializedName("MODELO")
    private String modelo;

    @SerializedName("NUMERO_SERIE")
    private String numeroSerie;

    @SerializedName("COSTO")
    private Double costo;

    @SerializedName("NUMERO_FACTURA")
    private String numeroFactura;

    @SerializedName("FECHA_COMPRA")
    private String fechaCompra;

    @SerializedName("FECHA_CAPITALIZACION")
    private String fechaCapitalizacion;

    @SerializedName("VALOR_RESIDUAL")
    private Double valorResidual;

    @SerializedName("DOCUMENTO")
    private String documento;

    @SerializedName("FOTOS")
    private String fotos;

    @SerializedName("NUMERO_PARTE_FABRICANTE")
    private String numeroParteFabricante;

    @SerializedName("DEPRECIADO")
    private String depreciado;

    @SerializedName("DESCRIPCION_DEPRECIADO")
    private String descripcionDepreciado;

    @SerializedName("ANOS_VIDA_UTIL")
    private Integer anosVidaUtil;

    @SerializedName("CUENTA_CONTABLE_DEPRESIACION")
    private String cuentaContableDepresiacion;

    @SerializedName("CENTRO_COSTOS")
    private String centroCostos;

    @SerializedName("DESCRIPCION_ESTADO_ULTIMO_INVENTARIO")
    private String descripcionEstadoUltimoInventario;

    @SerializedName("TAG_EPC")
    private String tagEpc;

    @SerializedName("EMPLEADO")
    private String empleado;

    @SerializedName("UBICACION_A")
    private String ubicacionA;

    @SerializedName("UBICACION_B")
    private String ubicacionB;

    @SerializedName("UBICACION_C")
    private String ubicacionC;

    @SerializedName("UBICACION_D")
    private String ubicacionD;

    @SerializedName("UBICACION_SECUNDARIA")
    private String ubicacionSecundaria;

    @SerializedName("FECHA_GARANTIA")
    private String fechaGarantia;

    @SerializedName("COLOR")
    private String color;

    @SerializedName("TAMANIO_MEDIDA")
    private String tamanioMedida;

    @SerializedName("OBSERVACIONES")
    private String observaciones;

    @SerializedName("ESTADO_ACTIVO")
    private Boolean estadoActivo;

    @SerializedName("FECHA_CREACION_ACTIVO")
    private String fechaCreacionActivo;

    // --- Getters y Setters ---

    @NonNull
    public String getIdActivo() { return idActivo; }
    public void setIdActivo(@NonNull String idActivo) { this.idActivo = idActivo; }

    public String getNumeroActivo() { return numeroActivo; }
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
}
