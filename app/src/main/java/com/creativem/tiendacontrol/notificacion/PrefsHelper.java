package com.creativem.tiendacontrol.notificacion;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PrefsHelper {
    private static final String PREFS_NAME = "recordatorios_prefs";
    private static final String KEY_RECORDATORIOS = "lista_recordatorios";

    public static void guardarLista(Context context, List<RecordatorioModel> lista) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(lista);
        editor.putString(KEY_RECORDATORIOS, json);
        editor.apply();
    }

    public static List<RecordatorioModel> cargarLista(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_RECORDATORIOS, null);

        Type type = new TypeToken<ArrayList<RecordatorioModel>>() {}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }
}