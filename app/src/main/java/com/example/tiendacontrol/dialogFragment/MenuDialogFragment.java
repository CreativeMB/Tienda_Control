package com.example.tiendacontrol.dialogFragment;

import static androidx.core.app.ActivityCompat.finishAffinity;
import static com.google.common.reflect.Reflection.getPackageName;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.monitor.Database;
import com.example.tiendacontrol.adapter.MenuAdapter;
import com.example.tiendacontrol.monitor.Donar;
import com.example.tiendacontrol.monitor.Inicio;
import com.example.tiendacontrol.monitor.SetCode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class MenuDialogFragment extends BottomSheetDialogFragment {
    private ListView menuListView;
    private MenuAdapter menuAdapter;
    private List<MenuItemImpl> menuItems = new ArrayList<>();

    private FragmentActivity activity;

    public interface MainActivityListener {
        void confirmarEliminarTodo();
    }

    private MainActivityListener listener;

    public static MenuDialogFragment newInstance() {
        return new MenuDialogFragment();
    }

    public void setListener(MainActivityListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_dialog, container, false);

        menuListView = view.findViewById(R.id.menu_list);

        // Configurar el adaptador de menú personalizado
        menuAdapter = new MenuAdapter(requireContext(), menuItems);
        menuListView.setAdapter(menuAdapter);

        // Manejar clics en los elementos del menú
        menuListView.setOnItemClickListener((parent, view1, position, id) -> {
            MenuItemImpl menuItem = menuItems.get(position);
            handleMenuItemClick(menuItem);
            dismiss();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
                DisplayMetrics displayMetrics = new DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                int width = (int) (screenWidth * 0.6);
                layoutParams.width = width;
                bottomSheet.setLayoutParams(layoutParams);

                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                behavior.setFitToContents(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // Animación de entrada desde la izquierda
                bottomSheet.setTranslationX(-screenWidth);
                bottomSheet.animate()
                        .translationX(0)
                        .setDuration(300)
                        .start();
            }
        }
    }

    private void handleMenuItemClick(MenuItemImpl menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.inicio) {
            Intent intent = new Intent(requireContext(), Database.class);
            startActivity(intent);
//        } else if (id == R.id.code) {
//            Intent intent = new Intent(requireContext(), SetCode.class);
//            startActivity(intent);
//        } else if (id == R.id.dona) {
//            Intent intent = new Intent(requireContext(), Donar.class);
//            startActivity(intent);
//        } else if (id == R.id.salir) {
//            if (getActivity() != null) {
//                getActivity().finishAffinity();
//            }
        }
    }
}