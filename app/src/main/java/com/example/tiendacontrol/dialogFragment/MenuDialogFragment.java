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
import com.example.tiendacontrol.helper.BaseExporter;
import com.example.tiendacontrol.helper.BdHelper;
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
    private BaseExporter baseExporter; // Exportador de base de datos
    private FragmentActivity activity; // Variable para la Activity
    private static final String PREFS_NAME = "TiendaControlPrefs"; // Declare PREFS_NAME aquí
    private static final String KEY_CURRENT_DATABASE = "currentDatabase"; // Declare KEY_CURRENT_DATABASE aquí
    private String currentDatabase;
    private OnStoragePermissionResultListener storagePermissionResultListener; // Listener para permisos
    private BdHelper bdHelper; // Declarar bdHelper
    private final ActivityResultLauncher<Intent> manageAllFilesPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Aquí manejarías el resultado de la solicitud de permisos MANAGE_EXTERNAL_STORAGE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        if (storagePermissionResultListener != null) {
                            storagePermissionResultListener.onPermissionResult(true);
                        }
                    } else {
                        if (storagePermissionResultListener != null) {
                            storagePermissionResultListener.onPermissionResult(false);
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestWritePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                // Aquí manejarías el resultado de la solicitud de permisos WRITE_EXTERNAL_STORAGE
                if (storagePermissionResultListener != null) {
                    storagePermissionResultListener.onPermissionResult(granted);
                }
            }
    );

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

    public void setStoragePermissionResultListener(OnStoragePermissionResultListener listener) {
        this.storagePermissionResultListener = listener;
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

        if (id == R.id.nueva_venta) {
            // Mostrar el diálogo de nueva venta
            FragmentManager fragmentManager = getParentFragmentManager();
            // Crea una nueva instancia de IngresoDialogFragment con el nombre de la base de datos actual
            IngresoDialogFragment ingresoDialogFragment = IngresoDialogFragment.newInstance(currentDatabase);
            ingresoDialogFragment.show(fragmentManager, "ingreso_dialog");
        } else if (id == R.id.inicio) {
            // Regresar a la actividad anterior
            getActivity().finish();
        } else if (id == R.id.code) {
            // Ir a la pantalla de configuración de código
            Intent intent = new Intent(requireContext(), SetCode.class);
            startActivity(intent);
        } else if (id == R.id.FilBase) {
            // Ir a la pantalla de filtro por día, mes o año
            Intent intent = new Intent(requireContext(), FiltroDiaMesAno.class);
            startActivity(intent);
        } else if (id == R.id.basedatos) {
            // Simplemente inicia Database
            Log.d("ActivityLifecycle", "Starting Database");
            Intent intent = new Intent(requireContext(), Database.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else if (id == R.id.nuevo_gasto) {
            // Mostrar el diálogo de nuevo gasto
            FragmentManager fragmentManager = getParentFragmentManager();
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(fragmentManager, "GastoDialogFragment");


//        } else if (id == R.id.exportar_exel) {
//            BaseExporter baseExporter = new BaseExporter(requireContext()); // Utiliza requireContext()
//            if (baseExporter.isStoragePermissionGranted()) {
//                ExcelExporter.exportToExcel(requireContext(), bdVentas.obtenerVentas()); // Pasa la lista de ventas a exportar
//            } else {
//                requestStoragePermission(granted -> {
//                    if (granted) {
//                        ExcelExporter.exportToExcel(requireContext(), bdVentas.obtenerVentas()); // Pasa la lista de ventas a exportar
//                    } else {
//                        Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//        } else if (id == R.id.exportar_db) {
//            // Exportar base de datos
//            BaseExporter baseExporter = new BaseExporter(requireContext()); // Utiliza requireContext()
//            // Comprueba si ya se concedió el permiso
//            if (baseExporter.isStoragePermissionGranted()) {
//                baseExporter.exportDatabase(BdHelper.DATABASE_NAME);
//            } else {
//                // Si no se tiene el permiso, solicita permiso
//                requestStoragePermission(granted -> {
//                    if (granted) {
//                        baseExporter.exportDatabase(BdHelper.DATABASE_NAME); // Ejecuta la exportación si se concede el permiso
//                    } else {
//                        Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        } else if (id == R.id.importar_db) {
//            // Importar base de datos
//            BaseExporter baseExporter = new BaseExporter(requireContext()); // Utiliza requireContext()
//            // Comprueba si ya se concedió el permiso
//            if (baseExporter.isStoragePermissionGranted()) {
//                baseExporter.importDatabase(BdHelper.DATABASE_NAME);
//            } else {
//                // Si no se tiene el permiso, solicita permiso
//                requestStoragePermission(granted -> {
//                    if (granted) {
//                        baseExporter.importDatabase(BdHelper.DATABASE_NAME); // Ejecuta la importación si se concede el permiso
//                    } else {
//                        Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
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

    private void requestStoragePermission(OnStoragePermissionResultListener listener) {
        this.storagePermissionResultListener = listener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                if (storagePermissionResultListener != null) {
                    storagePermissionResultListener.onPermissionResult(true);
                }
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                manageAllFilesPermissionLauncher.launch(intent);
            }
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            if (storagePermissionResultListener != null) {
                storagePermissionResultListener.onPermissionResult(true);
            }
        }
    }
}