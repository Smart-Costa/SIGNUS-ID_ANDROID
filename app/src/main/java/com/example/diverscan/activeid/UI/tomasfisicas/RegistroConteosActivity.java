package com.example.diverscan.activeid.UI.tomasfisicas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diverscan.activeid.FotoActivo.EFotoActivo;
import com.example.diverscan.activeid.R;
import com.example.diverscan.activeid.data.local.dao.ActivoDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaDetallesDao;
import com.example.diverscan.activeid.data.local.dao.TomaFisicaTomasDao;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaDetallesEntity;
import com.example.diverscan.activeid.data.local.entity.TomaFisicaTomasEntity;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.example.diverscan.activeid.sqlite.FotoDBHelper;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistroConteosActivity extends AppCompatActivity {

    private Button tabResumen;
    private Button tabActivos;
    private View tabsView;
    private View viewResumen;
    private View viewActivos;
    private RecyclerView recyclerActivos;

    private TextView txtTotales;
    private TextView txtNoEncontrados;
    private TextView txtEncontrados;
    private TextView txtNoPertenecen;
    private TextView txtNoInventariados;
    private TextView txtTitulo;
    private TextView txtEscaneados;
    private TextView txtDeTotal;
    private CircularProgressIndicator kpiProgressCircle;
    private CircularProgressIndicator progressActivos;

    private String tomaFisicaId;
    private String idToma;
    private String numeroToma;
    private String estadoFiltro;
    private String tituloFiltro;

    private TomaFisicaDetallesDao detallesDao;
    private ActivoDao activoDao;
    private TomaFisicaTomasDao tomasDao;

    private final ActivosAdapter adapter = new ActivosAdapter();
    private final List<TomaFisicaDetallesEntity> filasRemotas = new ArrayList<>();
    private boolean remotoCargando;
    private String remotoIdToma;
    private Categoria remotoCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_conteos);

        tomaFisicaId = getIntent().getStringExtra("tomaFisicaId");
        idToma = getIntent().getStringExtra("idToma");
        numeroToma = getIntent().getStringExtra("numeroToma");
        estadoFiltro = getIntent().getStringExtra("estadoFiltro");
        tituloFiltro = getIntent().getStringExtra("tituloFiltro");

        Log.d("RegistroConteos", "onCreate: tomaFisicaId=" + tomaFisicaId + ", idToma=" + idToma + ", estadoFiltro=" + estadoFiltro);

        detallesDao = new TomaFisicaDetallesDao(this);
        activoDao = new ActivoDao(this);
        tomasDao = new TomaFisicaTomasDao(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        txtTitulo = findViewById(R.id.txtTitulo);
        if (numeroToma != null && !numeroToma.trim().isEmpty()) {
            txtTitulo.setText("Resumen de conteos - Toma " + numeroToma.trim());
        }
        if (tituloFiltro != null && !tituloFiltro.trim().isEmpty()) {
            txtTitulo.setText(tituloFiltro.trim());
        }

        tabResumen = findViewById(R.id.tabResumen);
        tabActivos = findViewById(R.id.tabActivos);
        tabsView = findViewById(R.id.tabsView);
        viewResumen = findViewById(R.id.viewResumen);
        viewActivos = findViewById(R.id.viewActivos);
        recyclerActivos = findViewById(R.id.recyclerActivos);

        txtTotales = findViewById(R.id.txtTotales);
        txtNoEncontrados = findViewById(R.id.txtNoEncontrados);
        txtEncontrados = findViewById(R.id.txtEncontrados);
        txtNoPertenecen = findViewById(R.id.txtNoPertenecen);
        txtNoInventariados = findViewById(R.id.txtNoInventariados);
        txtEscaneados = findViewById(R.id.txtEscaneados);
        txtDeTotal = findViewById(R.id.txtDeTotal);
        kpiProgressCircle = findViewById(R.id.kpiProgressCircle);
        progressActivos = findViewById(R.id.progressActivos);

        recyclerActivos.setLayoutManager(new LinearLayoutManager(this));
        recyclerActivos.setAdapter(adapter);

        tabResumen.setOnClickListener(v -> setTab(true));
        tabActivos.setOnClickListener(v -> setTab(false));

        Button btnVerNoEncontrados = findViewById(R.id.btnVerNoEncontrados);
        Button btnVerEncontrados = findViewById(R.id.btnVerEncontrados);
        Button btnVerNoPertenecen = findViewById(R.id.btnVerNoPertenecen);
        Button btnVerNoInventariados = findViewById(R.id.btnVerNoInventariados);

        btnVerNoEncontrados.setOnClickListener(v -> abrirListado(Categoria.ROJO, "No encontrados"));
        btnVerEncontrados.setOnClickListener(v -> abrirListado(Categoria.VERDE, "Encontrados"));
        btnVerNoPertenecen.setOnClickListener(v -> abrirListado(Categoria.AMARILLO, "No pertenecen"));
        btnVerNoInventariados.setOnClickListener(v -> abrirListado(Categoria.BLANCO, "No inventariados"));

        if (filtroCategoria() != null) {
            if (tabsView != null) tabsView.setVisibility(View.GONE);
            setTab(false);
        } else {
            setTab(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos();
        if (tomaFisicaId != null && !tomaFisicaId.trim().isEmpty()) {
            tomasDao.fetchAndSyncFromApi(tomaFisicaId.trim(), () -> runOnUiThread(this::cargarDatos));
        }
        detallesDao.fetchAndSyncFromApi(idToma, () -> runOnUiThread(this::cargarDatos));
    }

    private void setTab(boolean mostrarResumen) {
        viewResumen.setVisibility(mostrarResumen ? View.VISIBLE : View.GONE);
        viewActivos.setVisibility(mostrarResumen ? View.GONE : View.VISIBLE);

        tabResumen.setBackgroundResource(mostrarResumen ? R.drawable.btn_primary : R.drawable.btn_secondary_gray);
        tabActivos.setBackgroundResource(mostrarResumen ? R.drawable.btn_secondary_gray : R.drawable.btn_primary);

        tabResumen.setTextColor(ContextCompat.getColor(this, mostrarResumen ? R.color.blanco : R.color.nav_item_text_tint));
        tabActivos.setTextColor(ContextCompat.getColor(this, mostrarResumen ? R.color.nav_item_text_tint : R.color.blanco));
    }

    private void cargarDatos() {
        if (idToma == null || idToma.trim().isEmpty()) {
            adapter.setItems(new ArrayList<>());
            // ... (resto del código de empty state)
            return;
        }

        Categoria filtro = filtroCategoria();
        Log.d("RegistroConteos", "cargarDatos: filtro=" + (filtro != null ? filtro.name() : "null") + ", idToma=" + idToma);

        // Lógica de visibilidad de filas de resumen
        int visibilityResumen = (filtro != null) ? View.GONE : View.VISIBLE;
        
        if (txtTotales != null) txtTotales.setVisibility(visibilityResumen);
        
        View summaryContainer = findViewById(R.id.summaryContainer);
        if (summaryContainer != null) {
            summaryContainer.setVisibility(visibilityResumen);
        } else {
            // Fallback para compatibilidad si el XML no se actualizó correctamente (aunque debería)
            View rowNoEncontrados = findViewById(R.id.rowNoEncontrados);
            if (rowNoEncontrados != null) rowNoEncontrados.setVisibility(visibilityResumen);
            
            View rowEncontrados = findViewById(R.id.rowEncontrados);
            if (rowEncontrados != null) rowEncontrados.setVisibility(visibilityResumen);
            
            View rowNoPertenecen = findViewById(R.id.rowNoPertenecen);
            if (rowNoPertenecen != null) rowNoPertenecen.setVisibility(visibilityResumen);
            
            View rowNoInventariados = findViewById(R.id.rowNoInventariados);
            if (rowNoInventariados != null) rowNoInventariados.setVisibility(visibilityResumen);
        }

        if (filtro != null) {
            String idTomaValue = idToma.trim();
            boolean sameToma = remotoIdToma != null && remotoIdToma.equalsIgnoreCase(idTomaValue);
            boolean sameCat = remotoCategoria == filtro;

            if (!remotoCargando && (!sameToma || !sameCat)) {
                remotoCargando = true;
                filasRemotas.clear();
                adapter.setDao(activoDao);
                adapter.setItems(new ArrayList<>());
                setCargando(true);

                if (filtro == Categoria.BLANCO) {
                    detallesDao.fetchAndSyncFromApi(idTomaValue, () -> runOnUiThread(() -> {
                        List<TomaFisicaDetallesEntity> filas = cargarFilasBlancoDesdeLocal(idTomaValue);
                        remotoIdToma = idTomaValue;
                        remotoCategoria = filtro;
                        remotoCargando = false;
                        filasRemotas.clear();
                        filasRemotas.addAll(filas);
                        setCargando(false);
                        cargarDatos();
                    }));
                } else {
                    activoDao.fetchAndSyncActivosPorEstado(idTomaValue, filtro.name(), new ApiCallback<List<ActivoEntity>>() {
                        @Override
                        public void onComplete(ApiResponse<List<ActivoEntity>> response) {
                            List<TomaFisicaDetallesEntity> filas = new ArrayList<>();
                            if (response.success && response.data != null) {
                                for (ActivoEntity a : response.data) {
                                    if (a == null) continue;
                                    String activoId = a.getIdActivo();
                                    if (activoId == null || activoId.trim().isEmpty()) continue;
                                    TomaFisicaDetallesEntity d = new TomaFisicaDetallesEntity();
                                    d.setIdToma(idTomaValue);
                                    d.setActivoId(activoId.trim());
                                    d.setEstadoInventario(estadoInventarioDeCategoria(filtro));
                                    filas.add(d);
                                }
                            }
                            runOnUiThread(() -> {
                                Log.d("RegistroConteos", "Filas remotas recibidas: " + filas.size());
                                remotoIdToma = idTomaValue;
                                remotoCategoria = filtro;
                                remotoCargando = false;
                                filasRemotas.clear();
                                filasRemotas.addAll(filas);
                                setCargando(false);
                                cargarDatos();
                            });
                        }
                    });
                }
                return;
            }

            if (remotoCargando) {
                adapter.setDao(activoDao);
                adapter.setItems(new ArrayList<>(filasRemotas));
                setCargando(true);
                return;
            }

            adapter.setDao(activoDao);
            adapter.setItems(new ArrayList<>(filasRemotas));
            setCargando(false);
            return;
        }

        List<TomaFisicaDetallesEntity> detalles = detallesDao.getByIdToma(idToma.trim());
        if (detalles == null) detalles = new ArrayList<>();

        int total = 0;
        int rojo = 0;
        int verde = 0;
        int amarillo = 0;
        int blanco = 0;

        TomaFisicaTomasEntity resumen = tomasDao != null ? tomasDao.getByIdToma(idToma.trim()) : null;
        if (resumen != null) {
            int totalActivos = parseIntOrZero(resumen.getTotalActivos());
            int faltantes = parseIntOrZero(resumen.getFaltantes());
            int sobrantes = parseIntOrZero(resumen.getSobrantes());
            int escaneados = parseIntOrZero(resumen.getActivosLeidos());

            int encontrados = totalActivos - faltantes;
            if (encontrados < 0) encontrados = 0;

            total = totalActivos + sobrantes;
            if (total < 0) total = 0;

            rojo = Math.max(0, faltantes);
            verde = Math.max(0, encontrados);
            amarillo = Math.max(0, sobrantes);
            blanco = Math.max(0, total - (rojo + verde + amarillo));

            if (txtEscaneados != null) txtEscaneados.setText(String.valueOf(Math.max(0, escaneados)));
            if (txtDeTotal != null) txtDeTotal.setText("De " + Math.max(0, totalActivos));
            if (kpiProgressCircle != null) {
                int progress = 0;
                if (totalActivos > 0) {
                    progress = (int) Math.round((Math.min(escaneados, totalActivos) * 100.0) / totalActivos);
                    if (progress < 0) progress = 0;
                    if (progress > 100) progress = 100;
                }
                kpiProgressCircle.setProgress(progress);
            }
        } else {
            if (txtEscaneados != null) txtEscaneados.setText("0");
            if (txtDeTotal != null) txtDeTotal.setText("De 0");
            if (kpiProgressCircle != null) kpiProgressCircle.setProgress(0);
        }

        List<TomaFisicaDetallesEntity> filas = new ArrayList<>();
        for (TomaFisicaDetallesEntity d : detalles) {
            if (d == null) continue;
            if (d.getActivoId() == null && d.getEpc() == null) continue;
            if (filtro == null || categoriaDe(d.getEstadoInventario()) == filtro) {
                filas.add(d);
            }
            if (resumen == null) {
                total++;
                Categoria c = categoriaDe(d.getEstadoInventario());
                if (c == Categoria.ROJO) rojo++;
                else if (c == Categoria.VERDE) verde++;
                else if (c == Categoria.AMARILLO) amarillo++;
                else blanco++;
            }
        }

        txtTotales.setText("Total: " + total);
        txtNoEncontrados.setText(String.valueOf(rojo));
        txtEncontrados.setText(String.valueOf(verde));
        txtNoPertenecen.setText(String.valueOf(amarillo));
        txtNoInventariados.setText(String.valueOf(blanco));

        adapter.setDao(activoDao);
        adapter.setItems(filas);
    }

    private static int parseIntOrZero(String value) {
        if (value == null) return 0;
        String s = value.trim();
        if (s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private Categoria filtroCategoria() {
        if (estadoFiltro == null) return null;
        String s = estadoFiltro.trim();
        if (s.isEmpty()) return null;
        try {
            return Categoria.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return null;
        }
    }

    private void abrirListado(Categoria categoria, String titulo) {
        if (categoria == null) return;
        String displayTitle = titulo;
        if (numeroToma != null && !numeroToma.trim().isEmpty()) {
            displayTitle = titulo + " - Toma " + numeroToma.trim();
        }
        
        Log.d("RegistroConteos", "abrirListado: " + categoria.name() + ", idToma=" + idToma);

        Intent intent = new Intent(this, RegistroConteosActivity.class);
        intent.putExtra("tomaFisicaId", tomaFisicaId);
        intent.putExtra("idToma", idToma);
        intent.putExtra("numeroToma", numeroToma);
        intent.putExtra("estadoFiltro", categoria.name());
        intent.putExtra("tituloFiltro", displayTitle);
        startActivity(intent);
    }

    private enum Categoria { ROJO, VERDE, AMARILLO, BLANCO }

    private static Categoria categoriaDe(String estado) {
        if (estado == null) return Categoria.BLANCO;
        String s = estado.trim();
        if (s.isEmpty()) return Categoria.BLANCO;
        s = s.toUpperCase(Locale.ROOT);

        if (s.contains("VERDE") || s.contains("ENCONTRADO")) return Categoria.VERDE;
        if (s.contains("AMARILLO") || s.contains("SOBRANTE") || s.contains("NO PERTENECE") || s.contains("NO_PERTENECE") || s.contains("NOPERTENECE"))
            return Categoria.AMARILLO;
        if (s.contains("ROJO") || s.contains("FALTANTE") || s.contains("NO ENCONTRADO") || s.contains("NO_ENCONTRADO"))
            return Categoria.ROJO;
        if (s.contains("BLANCO") || s.contains("NO INVENTARIADO") || s.contains("PENDIENTE"))
            return Categoria.BLANCO;

        return Categoria.BLANCO;
    }

    private static String estadoInventarioDeCategoria(Categoria categoria) {
        if (categoria == Categoria.VERDE) return "ENCONTRADO";
        if (categoria == Categoria.AMARILLO) return "SOBRANTE";
        if (categoria == Categoria.ROJO) return "NO_ENCONTRADO";
        return "NO_INVENTARIADO";
    }

    private void setCargando(boolean cargando) {
        if (progressActivos != null) {
            progressActivos.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }
    }

    private List<TomaFisicaDetallesEntity> cargarFilasBlancoDesdeLocal(String idTomaValue) {
        List<TomaFisicaDetallesEntity> list = new ArrayList<>();
        List<TomaFisicaDetallesEntity> detalles = detallesDao.getByIdToma(idTomaValue);
        if (detalles == null) return list;

        for (TomaFisicaDetallesEntity d : detalles) {
            if (d == null) continue;
            String epc = d.getEpc();
            if (epc == null) continue;
            String epcTrim = epc.trim();
            if (epcTrim.isEmpty()) continue;
            if (epcTrim.equalsIgnoreCase("EPC Asignado")) continue;

            String activoId = d.getActivoId();
            if (activoId != null && !activoId.trim().isEmpty()) continue;

            TomaFisicaDetallesEntity row = new TomaFisicaDetallesEntity();
            row.setIdToma(idTomaValue);
            row.setEpc(epcTrim);
            row.setEstadoInventario(estadoInventarioDeCategoria(Categoria.BLANCO));
            list.add(row);
        }

        return list;
    }

    private static class ActivosAdapter extends RecyclerView.Adapter<ActivosAdapter.VH> {

        private final List<TomaFisicaDetallesEntity> items = new ArrayList<>();
        private final Map<String, ActivoEntity> cacheByKey = new HashMap<>();
        private final Map<String, Bitmap> bitmapCacheByKey = new HashMap<>();
        private ActivoDao activoDao;
        private FotoDBHelper fotoDb;

        void setDao(ActivoDao dao) {
            this.activoDao = dao;
        }

        void setItems(List<TomaFisicaDetallesEntity> nuevos) {
            items.clear();
            cacheByKey.clear();
            bitmapCacheByKey.clear();
            if (nuevos != null) items.addAll(nuevos);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (fotoDb == null) {
                fotoDb = new FotoDBHelper(parent.getContext());
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activo_estado, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            TomaFisicaDetallesEntity d = items.get(position);
            String epc = d.getEpc();
            String activoId = d.getActivoId();

            ActivoEntity activo = null;
            String lookupKey = null;
            if (epc != null && !epc.trim().isEmpty()) {
                lookupKey = "EPC:" + epc.trim();
            } else if (activoId != null && !activoId.trim().isEmpty()) {
                lookupKey = "ID_ACTIVO:" + activoId.trim();
            }

            if (activoDao != null && lookupKey != null) {
                if (cacheByKey.containsKey(lookupKey)) {
                    activo = cacheByKey.get(lookupKey);
                } else {
                    if (lookupKey.startsWith("EPC:")) {
                        activo = activoDao.getActivoByEpc(epc.trim());
                    } else {
                        activo = activoDao.getActivoByIdActivo(activoId.trim());
                    }
                    cacheByKey.put(lookupKey, activo);
                }
            }

            String nombre = normalizar(activo != null ? activo.getDescripcionCorta() : null);
            String valNumeroActivo = normalizar(activo != null ? activo.getNumeroActivo() : null);
            String valNumeroEtiqueta = normalizar(activo != null ? activo.getNumeroEtiqueta() : null);
            String serie = normalizar(activo != null ? activo.getNumeroSerie() : null);

            // Web Mapping: Placa -> NUMERO_ACTIVO, NoActivo -> NUMERO_ETIQUETA
            // Originalmente: txtPlaca -> NUMERO_ETIQUETA (fallback numeroActivo), txtNumeroActivo -> NUMERO_ACTIVO

            if (serie.isEmpty() && activo == null && epc != null && !epc.trim().isEmpty()) serie = epc.trim();

            holder.txtNombre.setText(nombre.isEmpty() ? "Sin nombre" : nombre);
            // Mostrar Etiqueta en "Activo:"
            holder.txtNumeroActivo.setText("Activo: " + (valNumeroEtiqueta.isEmpty() ? "-" : valNumeroEtiqueta));
            // Mostrar NumeroActivo en "Placa:"
            holder.txtPlaca.setText("Placa: " + (valNumeroActivo.isEmpty() ? "-" : valNumeroActivo));
            holder.txtSerie.setText("S/N: " + (serie.isEmpty() ? "-" : serie));

            holder.imgFoto.setImageResource(R.drawable.no_hay_foto);
            if (activo != null && lookupKey != null) {
                Bitmap cached = bitmapCacheByKey.get(lookupKey);
                if (cached != null) {
                    holder.imgFoto.setImageBitmap(cached);
                } else {
                    String fotoBase64 = extraerPrimeraFotoBase64(activo.getFotos());
                    Bitmap bmp = decodeBase64ToBitmap(fotoBase64);
                    if (bmp == null) {
                        String ruta = extraerPrimeraRutaFoto(activo.getIdActivo());
                        bmp = decodeFileToBitmap(ruta);
                    }
                    if (bmp != null) {
                        bitmapCacheByKey.put(lookupKey, bmp);
                        holder.imgFoto.setImageBitmap(bmp);
                    }
                }
            }

            Categoria c = categoriaDe(d.getEstadoInventario());
            int bg;
            int tColor;
            int t2Color;
            if (c == Categoria.ROJO) {
                bg = ContextCompat.getColor(holder.itemView.getContext(), R.color.rojo);
                tColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.blanco);
                t2Color = tColor;
            } else if (c == Categoria.VERDE) {
                bg = ContextCompat.getColor(holder.itemView.getContext(), R.color.verde);
                tColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.blanco);
                t2Color = tColor;
            } else if (c == Categoria.AMARILLO) {
                bg = ContextCompat.getColor(holder.itemView.getContext(), R.color.amarillo);
                tColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black);
                t2Color = tColor;
            } else {
                bg = ContextCompat.getColor(holder.itemView.getContext(), R.color.blanco);
                tColor = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black);
                t2Color = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray);
            }

            holder.itemView.setBackgroundColor(bg);
            holder.txtNombre.setTextColor(tColor);
            holder.txtNumeroActivo.setTextColor(t2Color);
            holder.txtSerie.setTextColor(t2Color);
            holder.txtPlaca.setTextColor(t2Color);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final ImageView imgFoto;
            final TextView txtNombre;
            final TextView txtNumeroActivo;
            final TextView txtSerie;
            final TextView txtPlaca;

            VH(@NonNull View itemView) {
                super(itemView);
                imgFoto = itemView.findViewById(R.id.imgFoto);
                txtNombre = itemView.findViewById(R.id.txtNombre);
                txtNumeroActivo = itemView.findViewById(R.id.txtNumeroActivo);
                txtSerie = itemView.findViewById(R.id.txtSerie);
                txtPlaca = itemView.findViewById(R.id.txtPlaca);
            }
        }

        private static String normalizar(String value) {
            if (value == null) return "";
            String s = value.trim();
            return s.isEmpty() ? "" : s;
        }

        private static String extraerPrimeraFotoBase64(String fotos) {
            if (fotos == null) return null;
            String s = fotos.trim();
            if (s.isEmpty()) return null;

            if (s.startsWith("data:")) {
                int idx = s.indexOf("base64,");
                if (idx >= 0) {
                    s = s.substring(idx + "base64,".length()).trim();
                }
                return s.isEmpty() ? null : s;
            }

            if (s.startsWith("[") && s.endsWith("]")) {
                s = s.substring(1, s.length() - 1).trim();
            }

            String[] candidates = s.split("[,;|\\n\\r]+");
            if (candidates.length == 0) return null;
            String c = candidates[0] != null ? candidates[0].trim() : "";
            if (c.startsWith("\"") && c.endsWith("\"") && c.length() >= 2) {
                c = c.substring(1, c.length() - 1).trim();
            }
            if (c.startsWith("data:")) {
                int idx = c.indexOf("base64,");
                if (idx >= 0) {
                    c = c.substring(idx + "base64,".length()).trim();
                }
            }
            return c.isEmpty() ? null : c;
        }

        private static Bitmap decodeBase64ToBitmap(String b64) {
            if (b64 == null) return null;
            String s = b64.trim();
            if (s.isEmpty()) return null;
            try {
                byte[] decoded = Base64.decode(s, Base64.DEFAULT);
                if (decoded == null || decoded.length == 0) return null;
                return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            } catch (Exception e) {
                return null;
            }
        }

        private String extraerPrimeraRutaFoto(String assetSysId) {
            if (fotoDb == null || assetSysId == null) return null;
            String key = assetSysId.trim();
            if (key.isEmpty()) return null;
            try {
                ArrayList<EFotoActivo> fotos = fotoDb.ObtenerFotoActivo(key);
                if (fotos == null || fotos.isEmpty()) return null;
                EFotoActivo f = fotos.get(0);
                if (f == null) return null;
                String ruta = f.getRutaFoto();
                if (ruta == null) return null;
                String s = ruta.trim();
                return s.isEmpty() ? null : s;
            } catch (Exception e) {
                return null;
            }
        }

        private static Bitmap decodeFileToBitmap(String ruta) {
            if (ruta == null) return null;
            String s = ruta.trim();
            if (s.isEmpty()) return null;
            try {
                return BitmapFactory.decodeFile(s);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
