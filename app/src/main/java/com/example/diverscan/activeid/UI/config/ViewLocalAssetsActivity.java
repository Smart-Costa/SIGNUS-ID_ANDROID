package com.example.diverscan.activeid.UI.config;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;

import java.util.List;
import java.util.ArrayList;

public class ViewLocalAssetsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LocalAssetsAdapter adapter;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvCount;
    private ActivoDao activoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_local_assets);

        activoDao = new ActivoDao(this);

        recyclerView = findViewById(R.id.recycler_view);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        tvCount = findViewById(R.id.tv_count);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initial load
        loadAssets();

        btnSearch.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.filter(etSearch.getText().toString());
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAssets() {
        new Thread(() -> {
            // Load all assets (might be heavy if thousands, but requested "ver activos en sqlite")
            // Ideally we should use pagination or Limit, but let's load all for now as per request
            List<ActivoEntity> assets = activoDao.getAllLocalActivos();
            
            runOnUiThread(() -> {
                if (assets != null) {
                    tvCount.setText("Total registros: " + assets.size());
                    adapter = new LocalAssetsAdapter(assets);
                    recyclerView.setAdapter(adapter);
                } else {
                    tvCount.setText("Total registros: 0");
                    Toast.makeText(this, "No se encontraron activos.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}