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

public class MenuCustomAdapter extends ArrayAdapter<MenuItemImpl> {

    private Context mContext;
    private List<MenuItemImpl> mMenuItems;

    public MenuCustomAdapter(Context context, List<MenuItemImpl> menuItems) {
        super(context, 0, menuItems);
        mContext = context;
        mMenuItems = menuItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(R.layout.list_item_menu, parent, false);
        }

        MenuItemImpl menuItem = mMenuItems.get(position);

        TextView menuTitleTextView = listItemView.findViewById(R.id.menu_title);
        menuTitleTextView.setText(menuItem.getTitle());

        return listItemView;
    }
}
