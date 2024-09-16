package com.creativem.tiendacontrol.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.creativem.tiendacontrol.model.Items;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class ItemManager {
    private static final String PREFS_NAME = "items_prefs";
    private static final String ITEMS_KEY = "items_key";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public ItemManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveItem(Items item) {
        List<Items> items = getItems();
        items.add(item);
        String json = gson.toJson(items);
        sharedPreferences.edit().putString(ITEMS_KEY, json).apply();
    }

    public List<Items> getItems() {
        String json = sharedPreferences.getString(ITEMS_KEY, null);
        Type type = new TypeToken<ArrayList<Items>>() {
        }.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    public void removeItem(Items itemToRemove) {
        List<Items> items = getItems();
        // Encontrar el ítem a eliminar
        for (Items item : items) {
            if (item.getProducto().equals(itemToRemove.getProducto()) && item.getValor() == itemToRemove.getValor()) {
                // Utiliza una combinación de atributos únicos para identificar el ítem
                items.remove(item);
                break;
            }
        }
        // Convertir la lista actualizada a JSON y guardarla en SharedPreferences
        String json = gson.toJson(items);
        sharedPreferences.edit().putString(ITEMS_KEY, json).apply();
    }
}