package com.example.diverscan.activeid.data.remote.api;

import android.content.Context;
import android.util.Log;

import com.example.diverscan.activeid.BuildConfig;
import com.example.diverscan.activeid.Utilities.NetworkUtils;
import com.example.diverscan.activeid.Utilities.SessionManager;
import com.example.diverscan.activeid.data.local.entity.ActivoEntity;
import com.example.diverscan.activeid.data.remote.response.ApiCallback;
import com.example.diverscan.activeid.data.remote.response.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ApiClient {
    private static final String TAG = "LOGIN_APICLIENT";
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static ApiClient instance;
    private final AsyncHttpClient client;
    private final Context context;
    private final Gson gson;

    private ApiClient(Context context) {
        this.context = context.getApplicationContext();
        this.client = new AsyncHttpClient();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ActivoEntity.class, new ActivoEntityDeserializer())
                .create();

        client.setMaxRetriesAndTimeout(2, 10_000);
        client.addHeader("Accept", "application/json");
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.client.removeAllHeaders();
            instance = null;
        }
    }

    /* Helpers */
    private void ensureAuthHeader() {
        client.removeHeader("Authorization");
        String token = SessionManager.getToken(context);
        if (token != null && !token.isEmpty() && !SessionManager.isTokenExpired(token)) {
            client.addHeader("Authorization", "Bearer " + token);
        }
    }

    private String buildUrl(String endpoint) {
        if (endpoint.startsWith("/")) endpoint = endpoint.substring(1);
        return BASE_URL + "/" + endpoint;
    }

    /* GET Factory */
    public <T> void get(String endpoint, final Type typeOfT, final ApiCallback<T> callback) {
        if (!NetworkUtils.isOnline(context)) {
            callback.onComplete(ApiResponse.failure("No network connection", -1));
            return;
        }

        ensureAuthHeader();
        String url = buildUrl(endpoint);
        Log.d(TAG, "GET " + url);

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String resp = new String(responseBody, StandardCharsets.UTF_8);
                    T data = gson.fromJson(resp, typeOfT);
                    callback.onComplete(ApiResponse.success(data, statusCode));
                } catch (Exception e) {
                    Log.e(TAG, "GET parse error", e);
                    callback.onComplete(ApiResponse.failure("Parse error: " + e.getMessage(), statusCode));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String body = null;
                if (responseBody != null) {
                    body = new String(responseBody, StandardCharsets.UTF_8);
                }
                String message = (error != null) ? error.getMessage() : "HTTP error";
                Log.e(TAG, "GET failed: " + statusCode + " body:" + body, error);
                callback.onComplete(ApiResponse.failure(message + (body != null ? " - " + body : ""), statusCode));
            }
        });
    }

    /* POST Factory with JSON */
    public <T> void post(String endpoint, Object bodyObject, final Type typeOfT, final ApiCallback<T> callback) {
        if (!NetworkUtils.isOnline(context)) {
            callback.onComplete(ApiResponse.failure("No network connection", -1));
            return;
        }

        ensureAuthHeader();
        String url = buildUrl(endpoint);
        try {
            String json = gson.toJson(bodyObject);
            //StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            //entity.setContentType("application/json");
            StringEntity entity = new StringEntity(json, "application/json", "UTF-8");

            Log.d(TAG, "POST " + url + " -> " + json);

            client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String resp = new String(responseBody, StandardCharsets.UTF_8);
                        T data = gson.fromJson(resp, typeOfT);
                        callback.onComplete(ApiResponse.success(data, statusCode));
                    } catch (Exception e) {
                        Log.e(TAG, "POST parse error", e);
                        callback.onComplete(ApiResponse.failure("Parse error: " + e.getMessage(), statusCode));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String body = null;
                    if (responseBody != null) {
                        body = new String(responseBody, StandardCharsets.UTF_8);
                    }
                    String message = (error != null) ? error.getMessage() : "HTTP error";
                    // Enhanced Logging
                    Log.e(TAG, "POST failed: " + statusCode + " body:" + body, error);
                    callback.onComplete(ApiResponse.failure(message + (body != null ? " - Body: " + body : ""), statusCode));
                }
            });

        } catch (Exception e) {
            callback.onComplete(ApiResponse.failure("Request error: " + e.getMessage(), -1));
        }
    }

    /* ---------- Utilities ---------- */
    public void clearAuth() {
        client.removeHeader("Authorization");
        SessionManager.clearSession(context);
    }

    public Gson getGson() {
        return gson;
    }

    private static final class ActivoEntityDeserializer implements JsonDeserializer<ActivoEntity> {
        @Override
        public ActivoEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ActivoEntity a = new ActivoEntity();
            if (json == null || !json.isJsonObject()) return a;

            JsonObject o = json.getAsJsonObject();
            Map<String, JsonElement> normalized = new HashMap<>();
            for (Map.Entry<String, JsonElement> e : o.entrySet()) {
                if (e.getKey() == null) continue;
                normalized.put(normalizeKey(e.getKey()), e.getValue());
            }

            String idActivo = getAsString(normalized, "idactivo");
            if (idActivo != null && !idActivo.trim().isEmpty()) a.setIdActivo(idActivo.trim());

            a.setNumeroActivo(getAsString(normalized, "numeroactivo"));
            a.setNumeroEtiqueta(getAsString(normalized, "numeroetiqueta"));
            a.setDescripcionCorta(getAsString(normalized, "descripcioncorta"));
            a.setDescripcionLarga(getAsString(normalized, "descripcionlarga"));
            a.setCategoria(getAsString(normalized, "categoria"));
            a.setEstado(getAsString(normalized, "estado"));
            a.setEmpresa(getAsString(normalized, "empresa"));
            a.setMarca(getAsString(normalized, "marca"));
            a.setModelo(getAsString(normalized, "modelo"));
            a.setNumeroSerie(getAsString(normalized, "numeroserie"));

            Double costo = getAsDouble(normalized, "costo");
            if (costo != null) a.setCosto(costo);

            a.setNumeroFactura(getAsString(normalized, "numerofactura"));
            a.setFechaCompra(getAsString(normalized, "fechacompra"));
            a.setFechaCapitalizacion(getAsString(normalized, "fechacapitalizacion"));

            Double valorResidual = getAsDouble(normalized, "valorresidual");
            if (valorResidual != null) a.setValorResidual(valorResidual);

            a.setDocumento(getAsString(normalized, "documento"));
            a.setFotos(getAsString(normalized, "fotos"));
            a.setNumeroParteFabricante(getAsString(normalized, "numeropartefabricante"));
            a.setDepreciado(getAsString(normalized, "depreciado"));
            a.setDescripcionDepreciado(getAsString(normalized, "descripciondepreciado"));

            Integer anosVidaUtil = getAsInt(normalized, "anosvidautil");
            if (anosVidaUtil != null) a.setAnosVidaUtil(anosVidaUtil);

            a.setCuentaContableDepresiacion(getAsString(normalized, "cuentacontabledepresiacion"));
            a.setCentroCostos(getAsString(normalized, "centrocostos"));
            a.setDescripcionEstadoUltimoInventario(getAsString(normalized, "descripcionestadoultimoinventario"));
            a.setTagEpc(getAsString(normalized, "tagepc"));
            a.setEmpleado(getAsString(normalized, "empleado"));

            a.setUbicacionA(getAsString(normalized, "ubicaciona"));
            a.setUbicacionB(getAsString(normalized, "ubicacionb"));
            a.setUbicacionC(getAsString(normalized, "ubicacionc"));
            a.setUbicacionD(getAsString(normalized, "ubicaciond"));
            a.setUbicacionSecundaria(getAsString(normalized, "ubicacionsecundaria"));

            a.setFechaGarantia(getAsString(normalized, "fechagarantia"));
            a.setColor(getAsString(normalized, "color"));
            a.setTamanioMedida(getAsString(normalized, "tamaniomedida"));
            a.setObservaciones(getAsString(normalized, "observaciones"));
            a.setFechaCreacionActivo(getAsString(normalized, "fechacreacionactivo"));

            Boolean estadoActivo = getAsBoolean(normalized, "estadoactivo");
            if (estadoActivo != null) a.setEstadoActivo(estadoActivo);

            a.setEpc(getAsString(normalized, "epc"));
            a.setCategoriaA(getAsString(normalized, "categoriaa"));
            a.setCategoriaB(getAsString(normalized, "categoriab"));
            a.setCategoriaC(getAsString(normalized, "categoriac"));
            a.setUbicacionLogicaA(getAsString(normalized, "ubicacionlogicaa"));
            a.setUbicacionLogicaB(getAsString(normalized, "ubicacionlogicab"));
            a.setUbicacionLogicaC(getAsString(normalized, "ubicacionlogicac"));
            a.setEntidadAsociada(getAsString(normalized, "entidadasociada"));

            Double costoDepreciacion = getAsDouble(normalized, "costodepreciacion");
            if (costoDepreciacion != null) a.setCostoDepreciacion(costoDepreciacion);

            return a;
        }

        private static String normalizeKey(String key) {
            return key.replace("_", "").trim().toLowerCase();
        }

        private static String getAsString(Map<String, JsonElement> map, String key) {
            JsonElement el = map.get(key);
            if (el == null || el.isJsonNull()) return null;
            try {
                String s = el.getAsString();
                if (s == null) return null;
                return s;
            } catch (Exception ignore) {
                try {
                    return String.valueOf(el);
                } catch (Exception ignore2) {
                    return null;
                }
            }
        }

        private static Double getAsDouble(Map<String, JsonElement> map, String key) {
            JsonElement el = map.get(key);
            if (el == null || el.isJsonNull()) return null;
            try {
                return el.getAsDouble();
            } catch (Exception ignore) {
                String s = getAsString(map, key);
                if (s == null) return null;
                try {
                    return Double.parseDouble(s);
                } catch (Exception ignore2) {
                    return null;
                }
            }
        }

        private static Integer getAsInt(Map<String, JsonElement> map, String key) {
            JsonElement el = map.get(key);
            if (el == null || el.isJsonNull()) return null;
            try {
                return el.getAsInt();
            } catch (Exception ignore) {
                String s = getAsString(map, key);
                if (s == null) return null;
                try {
                    return Integer.parseInt(s);
                } catch (Exception ignore2) {
                    return null;
                }
            }
        }

        private static Boolean getAsBoolean(Map<String, JsonElement> map, String key) {
            JsonElement el = map.get(key);
            if (el == null || el.isJsonNull()) return null;
            try {
                return el.getAsBoolean();
            } catch (Exception ignore) {
                String s = getAsString(map, key);
                if (s == null) return null;
                String v = s.trim().toLowerCase();
                if (v.equals("true") || v.equals("1") || v.equals("si") || v.equals("sí")) return true;
                if (v.equals("false") || v.equals("0") || v.equals("no")) return false;
                return null;
            }
        }
    }
}


/*

    *//* GetRol *//*
    public void getRolHH(RolCallback callback) {
        String currentToken = SessionManager.getToken(context);
        if (currentToken == null || currentToken.isEmpty() || SessionManager.isTokenExpired(currentToken)) {
            callback.onResponse(new ArrayList<>());
            return;
        }

        client.removeAllHeaders();
        client.addHeader("Authorization", "Bearer " + currentToken);

        String url = BASE_URL + "/GetRolHH";
        Log.d("LOGIN_DEBUG", "Consultando roles en: " + url);

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                try {
                    String resp = new String(responseBody, StandardCharsets.UTF_8);
                    Log.d("LOGIN_DEBUG", "Respuesta roles: " + resp);

                    List<RolHHEntity> roles = new ArrayList<>();

                    try {
                        JSONArray jsonArray = new JSONArray(resp);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject o = jsonArray.getJSONObject(i);
                            RolHHEntity r = new RolHHEntity(
                                    o.optString("IdRol"),
                                    o.optString("Page"),
                                    o.optString("Description"),
                                    o.optString("Username"),
                                    o.optString("UserSysId"),
                                    o.optBoolean("Esta_Bloqueado") // nota el guion bajo
                            );
                            roles.add(r);
                        }
                    } catch (JSONException ex) {
                        JSONObject json = new JSONObject(resp);
                        JSONObject result = json.optJSONObject("ObtenerRolHHResult");
                        if (result != null && result.optBoolean("State")) {
                            JSONArray data = result.optJSONArray("Data");
                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject o = data.getJSONObject(i);
                                    RolHHEntity r = new RolHHEntity(
                                            o.optString("IdRol"),
                                            o.optString("Page"),
                                            o.optString("Description"),
                                            o.optString("Username"),
                                            o.optString("UserSysId"),
                                            o.optBoolean("EstaBloqueado")
                                    );
                                    roles.add(r);
                                }
                            }
                        }
                    }

                    Log.d("LOGIN_DEBUG", "Roles sincronizados correctamente (" + roles.size() + ")");
                    callback.onResponse(roles);

                } catch (Exception e) {
                    Log.e("LOGIN_DEBUG", "Error procesando roles: " + e.getMessage(), e);
                    callback.onResponse(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("LOGIN_DEBUG", "Fallo consultando roles: " + error.getMessage());
                callback.onResponse(new ArrayList<>());
            }
        });
    }*/
