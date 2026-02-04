package com.example.diverscan.activeid.GeneralTag;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.diverscan.activeid.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ConfigurationReaderActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public interface KeyEventHandler {
        boolean onDispatchKeyEvent(KeyEvent event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_reader);

        // Setup Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Zebra");
                            break;
                        case 1:
                            tab.setText("iMin");
                            break;
                        case 2:
                            tab.setText("Datalogic");
                            break;
                    }
                }).attach();

        // Handle initial tab selection
        int initialTab = getIntent().getIntExtra("INITIAL_TAB", 0);
        if (initialTab >= 0 && initialTab < 3) {
            viewPager.setCurrentItem(initialTab, false);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof KeyEventHandler && fragment.isResumed()) {
                if (((KeyEventHandler) fragment).onDispatchKeyEvent(event)) {
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull androidx.fragment.app.FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ZebraFragment();
                case 1:
                    return new IminFragment();
                case 2:
                    return new DatalogicFragment();
                default:
                    return new ZebraFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
