package com.example.diverscan.activeid.Locate_Assets;

public class Model_Seleccion_CardView {
    private String text;
    private boolean isSelected = false;

    public Model_Seleccion_CardView(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


    public boolean isSelected() {
        return isSelected;
    }
}
