//package com.creativem.tiendacontrol.dialogFragment;
//
//import android.app.DatePickerDialog;
//import android.content.Context;
//import android.util.Log;
//import android.widget.DatePicker;
//
//import com.creativem.tiendacontrol.model.Items;
//import com.creativem.tiendacontrol.monitor.FiltroDiaMesAno;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
//
//public class FiltroDiaMesAnoDialogFragment extends DatePickerDialog {
//
//    private final OnDateSetListener dateSetListener;
//    private FiltroDiaMesAno filtroDiaMesAno;
//    private List<Items> itemsList;
//    private OnDateFilteredListener onDateFilteredListener;
//
//
//
//    public interface OnDateFilteredListener {
//        void onDatesFiltered(List<Items> items);
//    }
//    public void setOnDateFilteredListener(OnDateFilteredListener listener) {
//        this.onDateFilteredListener = listener;
//    }
//
//    public void setItemsList(List<Items> itemsList) {
//        this.itemsList = itemsList;
//    }
//
//
//    public FiltroDiaMesAnoDialogFragment(Context context, OnDateSetListener listener, int year, int month, int dayOfMonth) {
//        super(context, listener, year, month, dayOfMonth);
//        this.dateSetListener = listener;
//        filtroDiaMesAno = new FiltroDiaMesAno();
//    }
//
//
//    @Override
//    protected void onStop() {
//        // No hacer nada aquí para evitar que el diálogo se cierre automáticamente.
//    }
//
//    @Override
//    public void onDateChanged(DatePicker view, int year, int month, int dayOfMonth) {
//        super.onDateChanged(view, year, month, dayOfMonth);
//
//        // Obtener la fecha seleccionada
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month, dayOfMonth);
//        Date selectedDate = calendar.getTime();
//
//
//        // Llama automáticamente al oyente cuando se selecciona una fecha
//        getDatePicker().clearFocus();
//        if (dateSetListener != null) {
//            dateSetListener.onDateSet(view, year, month, dayOfMonth);
//        }
//
//
//        if(itemsList != null){
//            filtroDiaMesAno.setDateFilterListener(itemsFiltered -> {
//                if(onDateFilteredListener != null){
//                    onDateFilteredListener.onDatesFiltered(itemsFiltered);
//                }
//
//                Log.d("FECHAS FILTRADAS", "Items Filtrados en Dialog: " + itemsFiltered.size());
//            });
//            filtroDiaMesAno.filterItemsByDate(itemsList,selectedDate );
//
//        }else{
//            Log.e("ERROR", "No hay lista de items para filtrar");
//        }
//
//        // Cierra automáticamente el diálogo cuando se selecciona la fecha
//        dismiss();
//    }
//
//
//}