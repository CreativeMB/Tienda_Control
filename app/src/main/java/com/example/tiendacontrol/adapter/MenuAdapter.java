package com.example.tiendacontrol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tiendacontrol.R;
import androidx.appcompat.view.menu.MenuItemImpl;

import java.util.List;

public class MenuAdapter extends ArrayAdapter<MenuItemImpl> {
    private Context mContext;  // Contexto de la aplicación
    private List<MenuItemImpl> mMenuItems;  // Lista de elementos de menú

    public MenuAdapter(Context context, List<MenuItemImpl> menuItems) {
        super(context, 0, menuItems);  // Llama al constructor de ArrayAdapter
        mContext = context;  // Inicializa el contexto
        mMenuItems = menuItems;  // Inicializa la lista de elementos de menú
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Infla una nueva vista si no hay una reciclada disponible
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(R.layout.lista_item_menu, parent, false);
        }

        // Obtiene el elemento de menú para la posición actual
        MenuItemImpl menuItem = mMenuItems.get(position);

        // Encuentra y establece el título del menú
        TextView menuTitleTextView = listItemView.findViewById(R.id.menu_title);
        menuTitleTextView.setText(menuItem.getTitle());

        return listItemView;  // Devuelve la vista para el elemento de menú
    }
}