package com.example.tiendacontrol.dialogFragment;

import static com.google.common.reflect.Reflection.getPackageName;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.monitor.Database;
import com.example.tiendacontrol.adapter.MenuCustomAdapter;


import com.example.tiendacontrol.login.Login;
import com.example.tiendacontrol.monitor.FiltroDiaMesAno;
import com.example.tiendacontrol.monitor.SetCode;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MenuDialogFragment extends BottomSheetDialogFragment {
    private ListView menuListView; // Vista para mostrar los elementos del menú
    private MenuCustomAdapter menuAdapter; // Adaptador para los elementos del menú
    private List<MenuItemImpl> menuItems = new ArrayList<>(); // Lista de elementos del menú

    private FragmentActivity activity; // Variable para la Activity
    private static final String PREFS_NAME = "TiendaControlPrefs"; // Declare PREFS_NAME aquí
    private static final String KEY_CURRENT_DATABASE = "currentDatabase"; // Declare KEY_CURRENT_DATABASE aquí
    private String currentDatabase;


    public interface MainActivityListener { // Interfaz para la comunicación
        void confirmarEliminarTodo();
    }

    private MainActivityListener listener; // Referencia al listener de MainActivity

    public static MenuDialogFragment newInstance() {
        return new MenuDialogFragment(); // Crear una nueva instancia del fragmento
    }

    public void setListener(MainActivityListener listener) {
        this.listener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_dialog, container, false); // Inflar el diseño del fragmento

        menuListView = view.findViewById(R.id.menu_list); // Obtener la vista de la lista del menú

        // Crear un MenuBuilder y cargar el menú desde XML
        MenuBuilder menuBuilder = new MenuBuilder(requireContext());
        MenuInflater menuInflater = new MenuInflater(requireContext());
        menuInflater.inflate(R.menu.menudialog, menuBuilder);

        // Obtener los elementos del menú como una lista de MenuItemImpl
        menuItems = getMenuItemsFromMenuBuilder(menuBuilder);

        // Crear el adaptador de menú personalizado
        menuAdapter = new MenuCustomAdapter(requireContext(), menuItems);
        menuListView.setAdapter(menuAdapter);

        // Manejar clics en los elementos del menú
        menuListView.setOnItemClickListener((parent, view1, position, id) -> {
            MenuItemImpl menuItem = menuItems.get(position); // Obtener el elemento del menú clicado
            handleMenuItemClick(menuItem); // Manejar el clic en el elemento del menú

            // Cerrar el diálogo al seleccionar un elemento del menú
            dismiss();
        });

        return view;
    }

    public interface OnStoragePermissionResultListener {
        void onPermissionResult(boolean granted);
    }

    // Método para obtener los elementos del menú como una lista de MenuItemImpl
    private List<MenuItemImpl> getMenuItemsFromMenuBuilder(MenuBuilder menuBuilder) {
        List<MenuItemImpl> items = new ArrayList<>();
        int size = menuBuilder.size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = (MenuItemImpl) menuBuilder.getItem(i);
            if (item.isVisible()) {
                items.add(item);
            }
        }
        return items;
    }

    // Método para manejar el clic en los elementos del menú
    private void handleMenuItemClick(MenuItemImpl menuItem) {
        int id = menuItem.getItemId(); // Obtener el ID del elemento del menú

        if (id == R.id.nuevo_Ingreso) {
            // Mostrar el diálogo de nueva venta
            FragmentManager fragmentManager = getParentFragmentManager();
            // Crea una nueva instancia de IngresoDialogFragment con el nombre de la base de datos actual
            IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance(currentDatabase);
            ingresoDialogFragment.show(fragmentManager, "nuevo_Ingreso");
        } else if (id == R.id.nuevo_Egreso) {
            // Mostrar el diálogo de nuevo gasto
            FragmentManager fragmentManager = getParentFragmentManager();
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(fragmentManager, "nuevo_Egreso");
        } else if (id == R.id.inicio) {
            // Regresar a la actividad anterior
            getActivity().finish();
        } else if (id == R.id.code) {
            // Ir a la pantalla de configuración de código
            Intent intent = new Intent(requireContext(), SetCode.class);
            startActivity(intent);
        } else if (id == R.id.cerrar_sesion) {
            // Cerrar sesión
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), Login.class);
            startActivity(intent);
        } else if (id == R.id.borrardados) {
            showDeleteConfirmationDialog();
        }
    }

    // Método para mostrar el diálogo de confirmación de eliminación de base de datos
    private void showDeleteConfirmationDialog() {
        // Verificar si el fragmento está adjunto a una actividad antes de continuar
        if (!isAdded()) {
            Log.e("MenuDialogFragment", "Fragmento no está adjunto a una actividad");
            return;
        }
        // Verificar si el listener de MainActivity no es nulo antes de llamar al método para eliminar la base de datos
        if (listener != null) {
            listener.confirmarEliminarTodo(); // Llamar al método para eliminar la base de datos
        } else {
            Log.e("MenuDialogFragment", "El listener de MainActivity es nulo");
        }
    }
}