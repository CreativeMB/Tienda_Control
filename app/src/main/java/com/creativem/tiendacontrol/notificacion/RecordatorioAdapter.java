package com.creativem.tiendacontrol.notificacion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.creativem.tiendacontrol.R;

import java.util.List;

public class RecordatorioAdapter extends RecyclerView.Adapter<RecordatorioAdapter.RecordatorioViewHolder> {

    private Context context;
    private List<RecordatorioModel> recordatorios;
    private OnRecordatorioListener listener;

    public interface OnRecordatorioListener {
        void onEliminar(RecordatorioModel recordatorio);
        void onSwitchChange(RecordatorioModel recordatorio, boolean activo);
    }

    public RecordatorioAdapter(Context context, List<RecordatorioModel> recordatorios, OnRecordatorioListener listener) {
        this.context = context;
        this.recordatorios = recordatorios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecordatorioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recordatorio, parent, false);
        return new RecordatorioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordatorioViewHolder holder, int position) {
        RecordatorioModel recordatorio = recordatorios.get(position);

        holder.txtTitulo.setText(recordatorio.getTitulo());
        holder.txtHora.setText(recordatorio.getHora());
        holder.txtHora.setSelected(true); // <-- aquí usamos holder.txtHora

        holder.txtRepeticion.setText(recordatorio.getRepeticion());
        holder.switchActivo.setChecked(recordatorio.isActivo());

        // Eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) listener.onEliminar(recordatorio);
        });

        // Activar/Desactivar
        holder.switchActivo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onSwitchChange(recordatorio, isChecked);
        });
    }


    @Override
    public int getItemCount() {
        return recordatorios.size();
    }

    public void actualizarLista(List<RecordatorioModel> nuevaLista) {
        this.recordatorios = nuevaLista;
        notifyDataSetChanged();
    }

    public static class RecordatorioViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtHora, txtRepeticion;
        Switch switchActivo;
        ImageButton btnEliminar;

        public RecordatorioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtHora = itemView.findViewById(R.id.txtHora);
            txtRepeticion = itemView.findViewById(R.id.txtRepeticion);
            switchActivo = itemView.findViewById(R.id.switchActivo);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}