package com.example.diverscan.activeid.Inventory;

import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.example.diverscan.activeid.TomasFisicas.TomasFisias;
import com.example.diverscan.activeid.TomasFisicas.EntidadActivosInventarios;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.sqlite.AssetsDBHelper;
import com.example.diverscan.activeid.sqlite.InventoryDBHelper;
import com.example.diverscan.activeid.DeviceInterface.ReaderTag;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class ChequearInventario {

    private Map<String, InventarioVisual> _activosUbicacion = new HashMap<String, InventarioVisual>();
    private Map<String, InventarioVisual> _activosSobrantes = new HashMap<String, InventarioVisual>();
    private Map<String, InventarioVisual> _activosEncontrado = new HashMap<String, InventarioVisual>();
    private Map<String, InventarioVisual> _activosBarcode = new HashMap<String, InventarioVisual>();
    ArrayList<InventarioVisual> _activoSinTag = new ArrayList<InventarioVisual>();
    public ArrayList<String> _activosEncontradosInsertar = new ArrayList<String>();
    Map<String, String> _tags = new HashMap<String, String>();
    String _IdTomaFisica;
    Context _context;
    ActivoDao activoDao;


    public ChequearInventario(ArrayList<TomasFisias> activos, String idTomaFisica,
                              Context context, IChequearInventario iChequearInventario){
        if (activos == null) {
            activos = new ArrayList<>();
        }

        ListToDictionary(activos, iChequearInventario);
        this._IdTomaFisica = idTomaFisica;
        _context= context;
        this.activoDao = new ActivoDao(context);
    }

    private void ListToDictionary(ArrayList<TomasFisias> activos, IChequearInventario iChequearInventario){
        for (TomasFisias activo : activos){
            InventarioVisual inventarioVisual = new InventarioVisual(
                    activo.getNumero(),
                    activo.getDescripcion(),
                    "Faltante",
                    activo.getEPC(),
                    activo.getAssetSysId(),
                    activo.getOficina(),
                    activo.getIdOficina(),
                    activo.getIdPiso(),
                    activo.getIdEdificio(),
                    activo.getIdCompania(),
                    activo.getUbicacionSecundaria()
            );
            if (activo.getEPC().equals("Sin Asignar")) {
                _activoSinTag.add(inventarioVisual);
                _activosBarcode.put(activo.getAssetSysId(), inventarioVisual);
            } else {
                _activosUbicacion.put(activo.getAssetSysId(), inventarioVisual);
            }

            iChequearInventario.RetornarCargaInicial(inventarioVisual);
        }
    }
    public boolean CheckTagsInventario(ReaderTag[] tagDatas, IChequearInventario iChequearInventario) {
        try {
            if (tagDatas == null || tagDatas.length == 0) {
                return false;
            }

            boolean isFound = false;

            for (ReaderTag tagData : tagDatas) {
                String epc = tagData.getEpc();
                if (epc.isEmpty()) {
                    continue;
                }
                
                // Buscamos coincidencia exacta con lo que hay en BD (que ya sabemos es HexString)
                if (!_tags.containsKey(epc)) {
                     boolean found = TryAddActivo(epc, iChequearInventario);
                     if (found) {
                         _tags.put(epc, epc);
                         isFound = true;
                     }
                }
            }
            return isFound;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean TryAddActivo(String epc, IChequearInventario iChequearInventario) {
         return AgregarActivos(epc, iChequearInventario);
    }

    public boolean AgregarActivos(String epc, IChequearInventario iChequearInventario) {
        try {
            EntidadActivosInventarios entidadActivos = activoDao.getActivoInventarioByEpc(epc);
            
            if (entidadActivos == null) {
                AssetsDBHelper assetsDBHelper = new AssetsDBHelper(_context);
                entidadActivos = assetsDBHelper.ActivosUbicacionInventario(epc);
            }

            if (entidadActivos != null) {
                InventarioVisual inventarioVisual = new InventarioVisual();

                if(_activosEncontrado.containsKey(entidadActivos.getAssetSysId())){
                    return true;
                }

                if(_activosSobrantes.containsKey(entidadActivos.getAssetSysId())){
                    return true;
                }

                if (_activosUbicacion.containsKey(entidadActivos.getAssetSysId())) {
                    //region Activos Encontrados
                    _activosEncontrado.put(entidadActivos.getAssetSysId(), inventarioVisual);
                    inventarioVisual.setAssetSysId(entidadActivos.getAssetSysId());
                    inventarioVisual.setDescripcion(entidadActivos.getDescripcion());
                    inventarioVisual.setNumero(entidadActivos.getNumero());
                    inventarioVisual.setOficina(entidadActivos.getOficina());
                    inventarioVisual.setIdOficina(entidadActivos.getIdOficina());
                    inventarioVisual.setIdPiso(entidadActivos.getIdPiso());
                    inventarioVisual.setIdEdificio(entidadActivos.getIdEdificio());
                    inventarioVisual.setIdCompania(entidadActivos.getIdCompania());
                    inventarioVisual.setUbicacionSecundaria(entidadActivos.getUbicacionSecundaria());
                    inventarioVisual.setEPC(entidadActivos.getEPC());
                    inventarioVisual.setStatus("Encontrado");
                    _activosUbicacion.remove(entidadActivos.getAssetSysId());
                    this._activosEncontradosInsertar.add(entidadActivos.getAssetSysId());
                    //endregion
                }else {
                    //region Activos que no pertenecen
                    inventarioVisual.setAssetSysId(entidadActivos.getAssetSysId());
                    inventarioVisual.setDescripcion(entidadActivos.getDescripcion());
                    inventarioVisual.setNumero(entidadActivos.getNumero());
                    inventarioVisual.setOficina(entidadActivos.getOficina());
                    inventarioVisual.setIdOficina(entidadActivos.getIdOficina());
                    inventarioVisual.setIdPiso(entidadActivos.getIdPiso());
                    inventarioVisual.setIdEdificio(entidadActivos.getIdEdificio());
                    inventarioVisual.setIdCompania(entidadActivos.getIdCompania());
                    inventarioVisual.setUbicacionSecundaria(entidadActivos.getUbicacionSecundaria());
                    inventarioVisual.setEPC(entidadActivos.getEPC());
                    inventarioVisual.setStatus("No Pertenece");
                    _activosSobrantes.put(entidadActivos.getAssetSysId(), inventarioVisual);
                    //endregion
                }
                // Aquí guardamos el EPC con el que SE ENCONTRÓ en la BD (entidadActivos.getEPC())
                // Si se encontró convirtiendo a ASCII, se guardará el ASCII.
                InsertarDetalleInventario(inventarioVisual.getEPC());

                iChequearInventario.RetornarActivo(inventarioVisual);
                return true;

            } else {
                // Activo No Inventariado (No existe en BD local)
                // Lo agregamos a Sobrantes con estado "No Inventariado"
                if (!_activosSobrantes.containsKey(epc) && !_activosEncontrado.containsKey(epc)) {
                    InventarioVisual inventarioVisual = new InventarioVisual();
                    inventarioVisual.setEPC(epc);
                    inventarioVisual.setDescripcion("No Inventariado");
                    inventarioVisual.setAssetSysId(UUID.randomUUID().toString()); // ID temporal
                    inventarioVisual.setStatus("No Inventariado");
                    
                    _activosSobrantes.put(epc, inventarioVisual); // Usamos EPC como Key si no hay AssetID
                    
                    // CORRECCION: No guardar en BD si no existe el activo (evitar saturacion)
                    // InsertarDetalleInventario(epc);
                    iChequearInventario.RetornarActivo(inventarioVisual);
                    return true;
                }
            }
            return false;

        } catch (final Exception e) {
            Log.e("ChequearInventario", "Error en AgregarActivos: ", e);
            return false;
        }
    }

    public void AgregarActivosBarcode(String Placa, IChequearInventario iChequearInventario) {
        try {
            EntidadActivosInventarios entidadActivos = activoDao.getActivoInventarioByBarcode(Placa);
            
            if (entidadActivos == null) {
                AssetsDBHelper assetsDBHelper = new AssetsDBHelper(_context);
                entidadActivos = assetsDBHelper.ActivosUbicacionInventarioBarcode(Placa);
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!Objects.isNull(entidadActivos)) {
                    InventarioVisual inventarioVisual = new InventarioVisual();

                    if(_activosEncontrado.containsKey(entidadActivos.getAssetSysId())){
                        return;
                    }
                    if(_activosSobrantes.containsKey(entidadActivos.getAssetSysId())){
                        return;
                    }

                    if(_activosBarcode.containsKey(entidadActivos.getAssetSysId())){
                        //region Activos Encontrados
                        _activosEncontrado.put(entidadActivos.getAssetSysId(), inventarioVisual);
                        inventarioVisual.setAssetSysId(entidadActivos.getAssetSysId());
                        inventarioVisual.setDescripcion(entidadActivos.getDescripcion());
                        inventarioVisual.setNumero(entidadActivos.getNumero());
                        inventarioVisual.setOficina(entidadActivos.getOficina());
                    inventarioVisual.setIdOficina(entidadActivos.getIdOficina());
                    inventarioVisual.setIdPiso(entidadActivos.getIdPiso());
                    inventarioVisual.setIdEdificio(entidadActivos.getIdEdificio());
                    inventarioVisual.setIdCompania(entidadActivos.getIdCompania());
                    inventarioVisual.setUbicacionSecundaria(entidadActivos.getUbicacionSecundaria());
                    inventarioVisual.setEPC(entidadActivos.getEPC());
                    inventarioVisual.setStatus("Encontrado");
                        _activosBarcode.remove(entidadActivos.getAssetSysId());
                        this._activosEncontradosInsertar.add(entidadActivos.getAssetSysId());
                        //endregion
                    }else if (_activosUbicacion.containsKey(entidadActivos.getAssetSysId())) {
                        //region Activos Encontrados
                        _activosEncontrado.put(entidadActivos.getAssetSysId(), inventarioVisual);
                        inventarioVisual.setAssetSysId(entidadActivos.getAssetSysId());
                        inventarioVisual.setDescripcion(entidadActivos.getDescripcion());
                        inventarioVisual.setNumero(entidadActivos.getNumero());
                        inventarioVisual.setOficina(entidadActivos.getOficina());
                    inventarioVisual.setIdOficina(entidadActivos.getIdOficina());
                    inventarioVisual.setIdPiso(entidadActivos.getIdPiso());
                    inventarioVisual.setIdEdificio(entidadActivos.getIdEdificio());
                    inventarioVisual.setIdCompania(entidadActivos.getIdCompania());
                    inventarioVisual.setUbicacionSecundaria(entidadActivos.getUbicacionSecundaria());
                    inventarioVisual.setEPC(entidadActivos.getEPC());
                    inventarioVisual.setStatus("Encontrado");
                        _activosUbicacion.remove(entidadActivos.getAssetSysId());
                        this._activosEncontradosInsertar.add(entidadActivos.getAssetSysId());
                        //endregion
                    }else{
                        //region Activos que no pertenecen
                        inventarioVisual.setAssetSysId(entidadActivos.getAssetSysId());
                        inventarioVisual.setDescripcion(entidadActivos.getDescripcion());
                        inventarioVisual.setNumero(entidadActivos.getNumero());
                        inventarioVisual.setOficina(entidadActivos.getOficina());
                    inventarioVisual.setIdOficina(entidadActivos.getIdOficina());
                    inventarioVisual.setIdPiso(entidadActivos.getIdPiso());
                    inventarioVisual.setIdEdificio(entidadActivos.getIdEdificio());
                    inventarioVisual.setIdCompania(entidadActivos.getIdCompania());
                    inventarioVisual.setUbicacionSecundaria(entidadActivos.getUbicacionSecundaria());
                    inventarioVisual.setEPC(entidadActivos.getEPC());
                    inventarioVisual.setStatus("No Pertenece");
                        _activosSobrantes.put(entidadActivos.getAssetSysId(), inventarioVisual);
                        //endregion
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        InsertarDetalleInventario(inventarioVisual.getEPC());
                    }
                    iChequearInventario.RetornarActivo(inventarioVisual);
                    return;
                }
            }

        } catch (final Exception e) {
            Log.e("ChequearInventario", "Error en AgregarActivosBarcode: ", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void InsertarDetalleInventario(String epcLeido) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        String fecha = dateFormat.format(date);
        try {
            String uniqueID = UUID.randomUUID().toString();
            InventoryDBHelper inventoryDBHelper = new InventoryDBHelper(_context);
            inventoryDBHelper.InsertarTomaDetalle(uniqueID, _IdTomaFisica, epcLeido, fecha);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void InsertarActivosEncontrados() {
        if (this._activosEncontradosInsertar.size() > 0) {

            try {
                for (Map.Entry<String, InventarioVisual> item : _activosEncontrado.entrySet()) {
                    InventarioVisual inventarioVisual = item.getValue();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        InsertarDetalleInventario(inventarioVisual.EPC);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    /*public ArrayList<String> tagDataToList(TagData[] epcs){

        ArrayList<String> newEpcs = new ArrayList<String>();

        for (int i = 0; i < epcs.length ; i++){
            newEpcs.add(epcs[i].getTagID());
        }
        return newEpcs;
    }*/



    public boolean CheckActivos(ArrayList<String> epcs) {
        if (epcs == null || epcs.isEmpty()){
            return false;
        }
        boolean isFound= false;

        for (String epc : epcs) {
            if(epc.isEmpty()) {
                continue;
            }
            if(_activosUbicacion.containsKey(epc)) {

                InventarioVisual inventarioVisual = _activosUbicacion.get(epc);
                inventarioVisual.setStatus("Encontrado");

                _activosEncontrado.put(epc, inventarioVisual);
                _activosUbicacion.remove(epc);
                this._activosEncontradosInsertar.add(epc);
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


    /*private void InsertarDetalleInventario(String epcLeido){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        Date date = new Date();
        String fecha = dateFormat.format(date);
        try{
            String uniqueID = UUID.randomUUID().toString();
            InventoryDBHelper inventoryDBHelper = new InventoryDBHelper(_context);
            inventoryDBHelper.InsertarTomaDetalle(uniqueID,_IdTomaFisica, epcLeido, fecha);

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }*/

    /*public void InsertarActivosEncontrados(){
        if (this._activosEncontradosInsertar.size()>0){

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = new Date();
            String fecha = dateFormat.format(date);

            try{
            for (String Epc : _activosEncontradosInsertar){
                String uniqueID = UUID.randomUUID().toString();
                InventoryDBHelper inventoryDBHelper = new InventoryDBHelper(_context);
                inventoryDBHelper.InsertarTomaDetalle(uniqueID, _IdTomaFisica,Epc, fecha);

                this._activosEncontradosInsertar.clear();

            }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }*/



    /*public void InsertarActivosEncontrados(){
        if (this._activosEncontradosInsertar.size()>0){

            try{

                    for(Map.Entry<String, InventarioVisual> item : _activosEncontrado.entrySet()){
                    InventarioVisual inventarioVisual = item.getValue();

                    InsertarDetalleInventario(inventarioVisual.EPC);

                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }*/



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
                InsertarDetalleInventario(inventarioVisual.getEPC());
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

    public void Clear()
    {
        _activosUbicacion.clear();
        _activosEncontrado.clear();
        _activosSobrantes.clear();
        _activoSinTag.clear();
    }

}
