package com.example.diverscan.activeid.ConfiguracionesGeneral;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SharedPreferencesGetSet extends Application {
    public static void guardar_local(String llave,
                                     String dato, Context _context) {
        try {
            SharedPreferences prf = _context.getSharedPreferences("Usuario", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prf.edit();
            editor.putString(llave,dato);
            editor.commit();
        } catch (Exception exc) {
        }
    }
    public static void guardar_Listalocal(String llave,
                                          ArrayList dato, Context _context) {
        try {
            SharedPreferences preferences = _context.getSharedPreferences("Usuario", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(dato);
            edit.putString(llave, json);
            edit.commit();
        } catch (Exception exc) {
            Log.d(exc.getMessage(), exc.getStackTrace().toString());
        }
    }

    public static String leer_listaLocal(String llave, Context _context) {
        try {
            SharedPreferences prf = _context.getSharedPreferences("Usuario",
                    Context.MODE_PRIVATE);

            return prf.getString(llave, "");
        } catch (Exception exc) {
            return "";
        }
    }
    public static String leer_local(String llave, Context _context) {
        try {
            SharedPreferences prf = _context.getSharedPreferences("Usuario",
                    Context.MODE_PRIVATE);
            return prf.getString(llave, "");
        } catch (Exception exc) {
            return "";
        }
    }
}
