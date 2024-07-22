package com.example.tiendacontrol.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.tiendacontrol.model.Items;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Type type = new TypeToken<ArrayList<Items>>() {}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }
}