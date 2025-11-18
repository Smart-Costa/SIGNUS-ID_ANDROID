package com.example.diverscan.activeid.CarruselCompleto;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class StepPagerAdapter extends FragmentStatePagerAdapter {
    private List<Step> stepList;

    public StepPagerAdapter(FragmentManager fm, List<Step> stepList) {
        super(fm);
        this.stepList = stepList;
    }

    @Override
    public Fragment getItem(int position) {
        Step step = stepList.get(position);

        return StepFragment.createFragment(step);
    }

    @Override
    public int getCount() {
        return stepList.size();
    }
}
