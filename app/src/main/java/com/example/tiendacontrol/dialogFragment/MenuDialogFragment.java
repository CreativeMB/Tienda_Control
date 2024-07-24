package com.example.tiendacontrol.dialogFragment;

import static com.example.tiendacontrol.monitor.MainActivity.REQUEST_CODE_STORAGE_PERMISSION;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.MenuCustomAdapter;
import com.example.tiendacontrol.helper.BaseExporter;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.helper.ExcelExporter;
import com.example.tiendacontrol.login.Login;
import com.example.tiendacontrol.login.PerfilUsuario;
import com.example.tiendacontrol.monitor.MainActivity;
import com.example.tiendacontrol.monitor.SetCode;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class MenuDialogFragment extends BottomSheetDialogFragment {
    private ListView menuListView; // Vista para mostrar los elementos del menú
    private MenuCustomAdapter menuAdapter; // Adaptador para los elementos del menú
    private List<MenuItemImpl> menuItems = new ArrayList<>(); // Lista de elementos del menú
    private BaseExporter baseExporter; // Exportador de base de datos

    public static MenuDialogFragment newInstance() {
        return new MenuDialogFragment(); // Crear una nueva instancia del fragmento
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

        if (id == R.id.nueva_venta) {
            // Mostrar el diálogo de nueva venta
            FragmentManager fragmentManager = getParentFragmentManager();
            IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance();
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
        } else if (id == R.id.inicio) {
            // Ir a la pantalla principal
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.code) {
            // Ir a la pantalla de configuración de código
            Intent intent = new Intent(requireContext(), SetCode.class);
            startActivity(intent);
        } else if (id == R.id.nuevo_gasto) {
            // Mostrar el diálogo de nuevo gasto
            FragmentManager fragmentManager = getParentFragmentManager();
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(fragmentManager, "GastoDialogFragment");
        } else if (id == R.id.perfil_usuario) {
            // Ir a la pantalla de perfil de usuario
            Intent intent = new Intent(requireContext(), PerfilUsuario.class);
            startActivity(intent);
        } else if (id == R.id.exportar_exel) {
            // Lógica para la opción "Exportar a Excel"
            if (isStoragePermissionGranted()) {
                // Suponiendo que ExcelExporter tenga un método estático
                ExcelExporter.exportToExcel(requireContext());
            }
        } else if (id == R.id.exportar_db) {
            // Lógica para la opción "Exportar Base de Datos"
            if (isStoragePermissionGranted()) {
                // Lógica para exportar la base de datos
                BaseExporter baseExporter = new BaseExporter(requireContext());
                baseExporter.exportDatabase(BdHelper.DATABASE_NAME);
            }
        } else if (id == R.id.importar_db) {
            // Lógica para la opción "Importar Base de Datos"
            if (isStoragePermissionGranted()) {
                // Lógica para importar la base de datos
                BaseExporter baseImporter = new BaseExporter(requireContext());
                baseImporter.importDatabase(BdHelper.DATABASE_NAME);
            }
        } else if (id == R.id.salir) {
            // Lógica para la opción "Salir"
            dirigirAInicioSesion();
            requireActivity().finish(); // Cierra la actividad actual
        }
    }

    // Método para verificar y solicitar permisos de almacenamiento
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                return true; // Permiso concedido
            } else {
                // Solicitar permiso MANAGE_EXTERNAL_STORAGE
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
                return false; // Permiso no concedido aún
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true; // Permiso concedido
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
                return false; // Permiso no concedido aún
            }
        } else {
            // Verifica si el permiso ya ha sido concedido
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true; // Permiso concedido
            } else {
                // Si el permiso no ha sido concedido, solicítalo al usuario
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
                // El resultado de la solicitud se manejará en onRequestPermissionsResult()
                return false; // Permiso no concedido aún
            }
        }
    }

    // Método para manejar el resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, realiza la operación que requiere el permiso
                Toast.makeText(requireContext(), "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show();
                // Realiza la operación que requería el permiso
            } else {
                // Permiso denegado, muestra un mensaje o realiza alguna acción adicional
                Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para dirigir al usuario a la pantalla de inicio de sesión
    private void dirigirAInicioSesion() {
        Intent intent = new Intent(requireContext(), Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finishAffinity(); // Cierra todas las actividades en la pila de tareas
    }
}