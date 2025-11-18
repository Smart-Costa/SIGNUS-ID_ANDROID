package com.example.diverscan.activeid.data.local.entity;

import androidx.annotation.NonNull;

import java.util.UUID;

public class ActivoEntity {
    @NonNull
    private String idActivo = UUID.randomUUID().toString();
    private String numeroActivo;
    private String numeroEtiqueta;
    private String descripcionCorta;
    private String descripcionLarga;
    private String categoria;
    private String estado;
    private String empresa;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private Double costo;
    private String numeroFactura;
    private String fechaCompra;
    private String fechaCapitalizacion;
    private Double valorResidual;
    private String documento;
    private String fotos;
    private String numeroParteFabricante;
    private String depreciado;
    private String descripcionDepreciado;
    private Integer anosVidaUtil;
    private String cuentaContableDepresiacion;
    private String centroCostos;
    private String descripcionEstadoUltimoInventario;
    private String tagEpc;
    private String empleado;
    private String ubicacionA;
    private String ubicacionB;
    private String ubicacionC;
    private String ubicacionD;
    private String ubicacionSecundaria;
    private String fechaGarantia;
    private String color;
    private String tamanioMedida;
    private String observaciones;
    private Boolean estadoActivo;
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
