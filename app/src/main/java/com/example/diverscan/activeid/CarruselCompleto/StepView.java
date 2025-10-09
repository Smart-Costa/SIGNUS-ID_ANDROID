package com.example.diverscan.activeid.CarruselCompleto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public abstract class StepView extends Fragment {

    Step step;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        step = getArguments().getParcelable("step");


    }
}
