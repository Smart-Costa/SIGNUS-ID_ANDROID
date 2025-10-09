package com.example.diverscan.activeid.Tags;

import android.content.Context;

import com.example.diverscan.activeid.sqlite.TagsDBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChequearTags  {

    Map<String, TagsVisual> _TagEncontrado    = new HashMap<String, TagsVisual>();
    Map<String, TagsVisual> _TagSinClasificar = new HashMap<String, TagsVisual>();
    Map<String, TagsVisual> _TagClasificado   = new HashMap<String, TagsVisual>();

    ArrayList<TagsVisual> _tagSinCategoria   = new ArrayList<TagsVisual>();
    Context _context;

    public ChequearTags(ArrayList<EntidadTagsClasificados> tags,  Context context){
        if(tags.size() <= 0)

            throw new NullPointerException();
        ListToDictionary(tags);
        _context= context;
    }

    private void ListToDictionary(ArrayList<EntidadTagsClasificados> tags){
        for (EntidadTagsClasificados tag : tags){

            if(tag.getTagTypeSysId().equals("00000000-0000-0000-0000-000000000000")){
                _tagSinCategoria.add(new TagsVisual(
                        tag.getTagSysId(),
                        tag.getTagID(),
                        tag.getTagTypeSysId(),
                        tag.getName(),
                        ("No Encontrado")

                ));
            }else{
                _TagEncontrado.put(
                        tag.getTagID(), new TagsVisual(
                                tag.getTagSysId(),
                                tag.getTagID(),
                                tag.getTagTypeSysId(),
                                tag.getName(),
                                "Clasificado")
                );
            }
        }
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
            if (_TagEncontrado.containsKey(epc)) {

                TagsVisual tagsVisual = _TagEncontrado.get(epc);
                tagsVisual.setEstado("Clasificado");

                _TagClasificado.put(epc, tagsVisual);
                _TagEncontrado.remove(epc);
                //this._activosEncontradosInsertar.add(epc);
                isFound=true;

            }else if(!_TagClasificado.containsKey(epc)){
                if(!_TagSinClasificar.containsKey(epc)){

                    TagsVisual sobrante = new TagsVisual();
                    sobrante.setTagID(epc);
                    sobrante.setEstado("No existe");
                    _TagSinClasificar.put(epc,sobrante);
                    isFound=true;
                }
            }
        }
        return isFound;
    }

    public void InsertarActivosEncontrados(){
        if (this._TagEncontrado.size()>0){

            try{
                for(Map.Entry<String, TagsVisual> item : _TagEncontrado.entrySet()){
                    TagsVisual tagsClasificados = item.getValue();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void ClearActivosSobrantes(){
        ArrayList<String> tagsToRemove = new ArrayList<String>();

        for(Map.Entry<String, TagsVisual> item : _TagSinClasificar.entrySet()){

            TagsVisual tagsVisual = item.getValue();
            if (tagsVisual == null){
                continue;
            }

            TagsDBHelper tagsDBHelper = new TagsDBHelper(_context);
            EntidadTagInventariados tagInventariados = tagsDBHelper.ObtenerInventarioTag(tagsVisual.getTagID());

            if(tagInventariados != null && !tagInventariados.getTagSysId().equals("")) {
                tagsVisual.setTagSysId(tagInventariados.getTagSysId());
                tagsVisual.setTagID(tagInventariados.getTagID());
                tagsVisual.setTagTypeSysId(tagInventariados.getTagTypeSysId());
                tagsVisual.setName(tagInventariados.getName());
                tagsVisual.setEstado("No Clasificado");
            }
        }

    }


    public  ArrayList<TagsVisual> GetActivos()
    {
        ArrayList<TagsVisual>  activosFinal= new  ArrayList<TagsVisual>();
        activosFinal.addAll(_TagClasificado.values());
        activosFinal.addAll(_TagEncontrado.values());
        activosFinal.addAll(_tagSinCategoria);
        activosFinal.addAll(_TagSinClasificar.values());
        return activosFinal;
    }

}
