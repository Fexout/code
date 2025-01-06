package com.ingat.obat;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReminverViewHolder extends RecyclerView.ViewHolder {
    TextView tv_nama, tv_dosis, tv_status, tv_waktu;
    Button btn_selesai;
    View view;

    public ReminverViewHolder(@NonNull View itemView) {
        super(itemView);

        tv_nama = itemView.findViewById(R.id.tv_nama);
        tv_dosis = itemView.findViewById(R.id.tv_dosis);
        tv_status = itemView.findViewById(R.id.tv_status);
        tv_waktu = itemView.findViewById(R.id.tv_waktu);
        btn_selesai = itemView.findViewById(R.id.btn_selesai);

        view = itemView;
    }
}
