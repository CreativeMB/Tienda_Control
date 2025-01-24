package com.creativem.tiendacontrol.dialogFragment;
import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

public class FiltroDiaMesAnoDialogFragment extends DatePickerDialog {

    private final OnDateSetListener dateSetListener;

    public FiltroDiaMesAnoDialogFragment(Context context, OnDateSetListener listener, int year, int month, int dayOfMonth) {
        super(context, listener, year, month, dayOfMonth);
        this.dateSetListener = listener;  // Guardamos el listener para usarlo más tarde
    }

    @Override
    protected void onStop() {
        // No hacer nada aquí para evitar que el diálogo se cierre automáticamente.
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int dayOfMonth) {
        super.onDateChanged(view, year, month, dayOfMonth);

        // Llama automáticamente al oyente cuando se selecciona una fecha
        getDatePicker().clearFocus();
        if (dateSetListener != null) {
            dateSetListener.onDateSet(view, year, month, dayOfMonth);
        }

        // Cierra automáticamente el diálogo cuando se selecciona la fecha
        dismiss();
    }

}