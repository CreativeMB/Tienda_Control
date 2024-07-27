package com.example.tiendacontrol.helper;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.tiendacontrol.model.Items;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class ItemManager {
    private static final String PREFS_NAME = "items_prefs"; // Nombre del archivo de preferencias compartidas
    private static final String ITEMS_KEY = "items_key"; // Clave para almacenar la lista de ítems
    private SharedPreferences sharedPreferences; // Objeto para acceder a las preferencias compartidas
    private Gson gson; // Objeto Gson para la conversión entre objetos y JSON

    public ItemManager(Context context) {
        // Inicializar SharedPreferences y Gson
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Método para guardar un ítem en las preferencias compartidas
    public void saveItem(Items item) {
        // Obtener la lista actual de ítems
        List<Items> items = getItems();
        // Agregar el nuevo ítem a la lista
        items.add(item);
        // Convertir la lista a JSON y guardarla en SharedPreferences
        String json = gson.toJson(items);
        sharedPreferences.edit().putString(ITEMS_KEY, json).apply();
    }

    // Método para obtener la lista de ítems desde las preferencias compartidas
    public List<Items> getItems() {
        // Obtener la cadena JSON desde SharedPreferences
        String json = sharedPreferences.getString(ITEMS_KEY, null);
        // Definir el tipo de la lista de ítems
        Type type = new TypeToken<ArrayList<Items>>() {}.getType();
        // Convertir la cadena JSON a una lista de ítems, o devolver una lista vacía si no hay datos
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    // Método para eliminar ítems personalizados y conservar solo los predefinidos
    public void removeCustomItems() {
        // Obtener la lista actual de ítems
        List<Items> items = getItems();
        List<Items> updatedItems = new ArrayList<>();

        // Filtrar y conservar solo los ítems predefinidos
        for (Items item : items) {
            if (item.isPredefined()) { // Asume que tienes un método para identificar ítems predefinidos
                updatedItems.add(item);
            }
        }

        // Convertir la lista actualizada a JSON y guardarla en SharedPreferences
        String json = gson.toJson(updatedItems);
        sharedPreferences.edit().putString(ITEMS_KEY, json).apply();
    }
}