package com.example.diverscan.activeid.Tags;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diverscan.activeid.R;

import java.util.List;

public class AdaptadorClasificacionTags extends RecyclerView.Adapter<AdaptadorClasificacionTags.ViewHolder> {
    private List<TagsVisual> _tagsVisuals;

    public AdaptadorClasificacionTags(List<TagsVisual> TagsVisuals){
        this._tagsVisuals = TagsVisuals;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements com.example.diverscan.activeid.Tags.ViewHolder{

        private TagsVisual tagsVisual;
        private TextView txtTag, txtCategoria, txtEstado;
        private CardView  Tarjeta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTag    =itemView.findViewById(R.id.txtEpcTag);
            txtCategoria =itemView.findViewById(R.id.txtCategoria);
            txtEstado =itemView.findViewById(R.id.txtEstado);
            Tarjeta = itemView.findViewById(R.id.tarjetaTags);
        }

        public TagsVisual getTagsVisual() {
            return tagsVisual;
        }

        public void setTagsVisual(TagsVisual product) {
            this.tagsVisual = product;
        }

        @Override
        public void onClick(View view) {

        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.campo_tag_clasificacion, viewGroup, false);
        AdaptadorClasificacionTags.ViewHolder viewHolder = new AdaptadorClasificacionTags.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final TagsVisual tagsVisual = _tagsVisuals.get(i);
        viewHolder.txtTag.setText(_tagsVisuals.get(i).getTagID());
        viewHolder.txtCategoria.setText(_tagsVisuals.get(i).getName());
        viewHolder.txtEstado.setText(_tagsVisuals.get(i).getEstado());
        viewHolder.setTagsVisual(_tagsVisuals.get(i));

    }

    @Override
    public int getItemCount() {
        return _tagsVisuals.size();
    }
}
