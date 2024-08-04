package com.example.tiendacontrol.dialogFragment;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static com.example.tiendacontrol.monitor.MainActivity.REQUEST_CODE_STORAGE_PERMISSION;
import static com.google.common.reflect.Reflection.getPackageName;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.tiendacontrol.R;
import com.example.tiendacontrol.adapter.MenuCustomAdapter;
import com.example.tiendacontrol.helper.BaseExporter;
import com.example.tiendacontrol.helper.BdHelper;
import com.example.tiendacontrol.helper.ExcelExporter;
import com.example.tiendacontrol.login.Login;
import com.example.tiendacontrol.login.PerfilUsuario;
import com.example.tiendacontrol.monitor.FiltroDiaMesAno;
import com.example.tiendacontrol.monitor.MainActivity;
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

    private OnStoragePermissionResultListener storagePermissionResultListener; // Listener para permisos

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
        } else if (id == R.id.FilBase) {
            // Ir a la pantalla de filtro por día, mes o año
            Intent intent = new Intent(requireContext(), FiltroDiaMesAno.class);
            startActivity(intent);
        } else if (id == R.id.nuevo_gasto) {
            // Mostrar el diálogo de nuevo gasto
            FragmentManager fragmentManager = getParentFragmentManager();
            GastoDialogFragment dialogFragment = new GastoDialogFragment();
            dialogFragment.show(fragmentManager, "GastoDialogFragment");
        } else if (id == R.id.exportar_exel) {
            // Exportar a Excel
            BaseExporter baseExporter = new BaseExporter(requireContext(), getActivity()); // Crea una instancia de BaseExporter
            if (baseExporter.isStoragePermissionGranted()) {
                ExcelExporter.exportToExcel(requireContext());
            } else {
                requestStoragePermission(granted -> {
                    if (granted) {
                        ExcelExporter.exportToExcel(requireContext());
                    } else {
                        Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } else if (id == R.id.exportar_db) {
            // Exportar base de datos
            BaseExporter baseExporter = new BaseExporter(requireContext(), getActivity()); // Crea una instancia de BaseExporter
            // Comprueba si ya se concedió el permiso
            if (baseExporter.isStoragePermissionGranted()) {
                baseExporter.exportDatabase(BdHelper.DATABASE_NAME);
            } else {
                // Si no se tiene el permiso, solicita permiso
              requestStoragePermission(granted -> {
                    if (granted) {
                        baseExporter.exportDatabase(BdHelper.DATABASE_NAME); // Ejecuta la exportación si se concede el permiso
                    } else {
                        Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (id == R.id.importar_db) {
            // Exportar base de datos
            BaseExporter baseExporter = new BaseExporter(requireContext(), getActivity()); // Crea una instancia de BaseExporter
            // Comprueba si ya se concedió el permiso
            if (baseExporter.isStoragePermissionGranted()) {
                baseExporter.importDatabase(BdHelper.DATABASE_NAME);
            } else {
                // Si no se tiene el permiso, solicita permiso
                requestStoragePermission(granted -> {
                            if (granted) {
            baseExporter.importDatabase(BdHelper.DATABASE_NAME);// Ejecuta la exportación si se concede el permiso
                            } else {
                                Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                            }
                });
            }
        } else if (id == R.id.salir) {
            // Salir de la aplicación
            signOut();
            requireActivity().finish(); // Cierra la actividad actual
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
    // Método para firmar al usuario
    private void signOut() {
        FirebaseAuth.getInstance().signOut(); // Cerrar sesión en Firebase
        Intent intent = new Intent(requireContext(), Login.class); // Crear la intención para ir a la pantalla de inicio de sesión
        startActivity(intent); // Iniciar la actividad de inicio de sesión
    }
    // Método para solicitar permisos de almacenamiento
    private void requestStoragePermission(OnStoragePermissionResultListener listener) {
        this.storagePermissionResultListener = listener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 y versiones posteriores - Solicitar permiso "MANAGE_EXTERNAL_STORAGE"
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                manageAllFilesPermissionLauncher.launch(intent);
            } else {
                if (listener != null) {
                    listener.onPermissionResult(true); // Permiso ya concedido
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 a Android 10 - Solicitar permiso "WRITE_EXTERNAL_STORAGE"
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                if (listener != null) {
                    listener.onPermissionResult(true); // Permiso ya concedido
                }
            }
        }
    }
    // Declarar los lanzadores de resultados de actividad
    private ActivityResultLauncher<Intent> manageAllFilesPermissionLauncher;
    private ActivityResultLauncher<String> requestWritePermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manageAllFilesPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            // Permiso concedido
                            if (storagePermissionResultListener != null) {
                                storagePermissionResultListener.onPermissionResult(true);
                            }
                        } else {
                            // Permiso denegado
                            if (storagePermissionResultListener != null) {
                                storagePermissionResultListener.onPermissionResult(false);
                            }
                        }
                    }
                }
        );

        requestWritePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permiso concedido
                        if (storagePermissionResultListener != null) {
                            storagePermissionResultListener.onPermissionResult(true);
                        }
                    } else {
                        // Permiso denegado
                        if (storagePermissionResultListener != null) {
                            storagePermissionResultListener.onPermissionResult(false);
                        }
                    }
                }
        );
    }

    // Interface para manejar el resultado del permiso de almacenamiento
    public interface OnStoragePermissionResultListener {
        void onPermissionResult(boolean granted);
    }
}