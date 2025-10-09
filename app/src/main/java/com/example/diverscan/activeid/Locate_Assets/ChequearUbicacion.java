package com.example.diverscan.activeid.Locate_Assets;

import android.content.Context;

import com.example.diverscan.activeid.Activo.ActivoInventario;
import com.example.diverscan.activeid.Activo.EntidadActivosInventarios;
import com.example.diverscan.activeid.Inventory.InventarioVisual;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChequearUbicacion {
    private Map<String, InventarioVisual> _activosUbicacion = new HashMap<String, InventarioVisual>();
    private Map<String, InventarioVisual> _activosSobrantes = new HashMap<String, InventarioVisual>();
    private Map<String, InventarioVisual> _activosEncontrado = new HashMap<String, InventarioVisual>();
    ArrayList<InventarioVisual> _activoSinTag = new ArrayList<InventarioVisual>();
    Context _context;

    public ChequearUbicacion(ArrayList<ActivoInventario> activos,  Context context){
        if(activos.size() <= 0)

            throw new NullPointerException();
        ListToDictionary(activos);
        _context= context;
    }

    private void ListToDictionary(ArrayList<ActivoInventario> activos){
        for (ActivoInventario activo : activos){

            if(activo.getEPC().equals("Sin Asignar")){
                _activoSinTag.add(new InventarioVisual(
                        activo.getNumero(),
                        activo.getDescripcion(),
                        "Faltante",
                        activo.getEPC(),
                        activo.getAssetSysId(),
                        activo.getOficina(),
                        activo.getIdOficina()
                ));
            }else{
                _activosUbicacion.put(
                        activo.getEPC(), new InventarioVisual(
                        activo.getNumero(),
                        activo.getDescripcion(),
                        "Faltante",
                        activo.getEPC(),
                        activo.getAssetSysId(),
                        activo.getOficina(),
                        activo.getIdOficina()   ));
            }
        }
    }


    public boolean CheckPlaca(ArrayList<String> Placas,String epc){
        if(Placas.equals(null) || Placas.size()==0){
            return false;
        }
        boolean isFound = false;
        for (String placa : Placas) {
            if (placa.isEmpty()) {
                continue;
            }
            if (_activosUbicacion.containsValue(placa)) {
                InventarioVisual inventarioVisual = _activosUbicacion.get(epc);
                inventarioVisual.setStatus("Encontrado");

                _activosEncontrado.put(epc, inventarioVisual);
                _activosUbicacion.remove(epc);
                isFound=true;
            } else if (!_activosEncontrado.containsKey(epc)) {
                if (!_activosSobrantes.containsKey(epc)) {

                    InventarioVisual sobrante = new InventarioVisual();
                    sobrante.setEPC(epc);
                    sobrante.setStatus("Sin Asignar");
                    _activosSobrantes.put(epc, sobrante);

                }
            }
        }
        return isFound;
    }

    public boolean CheckActivos(ArrayList<String> epcs) {
        if (epcs.equals(null) || epcs.size()==0) {
            return false;
        }
        boolean isFound= false;

        for (String epc : epcs) {
            if (epc.isEmpty()) {
                continue;
            }
            if (_activosUbicacion.containsKey(epc)) {

                InventarioVisual inventarioVisual = _activosUbicacion.get(epc);
                inventarioVisual.setStatus("Encontrado");

                _activosEncontrado.put(epc, inventarioVisual);
                _activosUbicacion.remove(epc);
                //this._activosEncontradosInsertar.add(epc);
                isFound=true;

            }else if(!_activosEncontrado.containsKey(epc)){
                if(!_activosSobrantes.containsKey(epc)){

                    InventarioVisual sobrante = new InventarioVisual();
                    sobrante.setEPC(epc);
                    sobrante.setStatus("Sin Asignar");
                    _activosSobrantes.put(epc,sobrante);
                    isFound=true;
                }
            }
        }
        return isFound;
    }

    public void InsertarActivosEncontrados(){
        if (this._activosEncontrado.size()>0){

            try{
                for(Map.Entry<String, InventarioVisual> item : _activosEncontrado.entrySet()){
                    InventarioVisual inventarioVisual = item.getValue();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void ClearActivosSobrantes(){
        ArrayList<String> tagsToRemove = new ArrayList<String>();

        for(Map.Entry<String, InventarioVisual> item : _activosSobrantes.entrySet()){

            InventarioVisual inventarioVisual = item.getValue();
            if (inventarioVisual== null){
                continue;
            }

            AssetsDBHelper assetsDBHelper = new AssetsDBHelper(_context);
            EntidadActivosInventarios entidadActivos = assetsDBHelper.ActivosUbicacionInventario(inventarioVisual.getEPC());

            if(entidadActivos != null && !entidadActivos.getAssetSysId().equals("")) {
                inventarioVisual.setAssetSysId(entidadActivos.getAssetSysId());
                inventarioVisual.setDescripcion(entidadActivos.getDescripcion());
                inventarioVisual.setNumero(entidadActivos.getNumero());
                inventarioVisual.setOficina(entidadActivos.getOficina());
                inventarioVisual.setStatus("No Pertenece");
            }
        }

    }


    public  ArrayList<InventarioVisual> GetActivos()
    {
        ArrayList<InventarioVisual>  activosFinal= new  ArrayList<InventarioVisual>();
        activosFinal.addAll(_activosEncontrado.values());
        activosFinal.addAll(_activosUbicacion.values());
        activosFinal.addAll(_activoSinTag);
        activosFinal.addAll(_activosSobrantes.values());
        return activosFinal;
    }
    public int cantidadActivosUbicacion()
    {
        return _activosUbicacion.size() + _activoSinTag.size() + _activosEncontrado.size();
    }

    public int cantidadActivosFaltantes()
    {
        return _activoSinTag.size() + _activosUbicacion.size();
    }

    public int cantidadActivosSobrantes()
    {
        return _activosSobrantes.size();
    }

    public int cantidadActivosEncontrados()
    {
        return _activosEncontrado.size();
    }

    public int cantidadEPCLeidos()
    {
        return _activosEncontrado.size() + _activosSobrantes.size();
    }

}
