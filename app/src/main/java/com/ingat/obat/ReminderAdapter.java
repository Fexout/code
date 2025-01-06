package com.ingat.obat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReminderAdapter extends RecyclerView.Adapter<ReminverViewHolder> {
    List<Map<String, Object>> data;
    Context context;
    ClickListener listener;

    public ReminderAdapter(Context context, List<Map<String, Object>> data, ClickListener listener) {
        this.data = data;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReminverViewHolder(View.inflate(context, R.layout.item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ReminverViewHolder holder, int position) {
        int index = holder.getAdapterPosition();

        holder.tv_nama.setText(Objects.requireNonNull(data.get(index).get("nama")).toString());
        holder.tv_dosis.setText("Dosis: " + Objects.requireNonNull(data.get(index).get("dosis")));
        holder.tv_status.setText(Objects.requireNonNull(data.get(index).get("status")).toString());
        holder.tv_waktu.setText(Objects.requireNonNull(data.get(index).get("tanggal")) + ":" + Objects.requireNonNull(data.get(index).get("waktu")));
        holder.btn_selesai.setOnClickListener(view -> listener.onItemClicked(index));

        if (data.get(index).get("sudah").equals(true))
            holder.btn_selesai.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
