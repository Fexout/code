package com.ingat.obat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JadwalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_jadwal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        getSupportActionBar().setTitle("Jadwal Minum Obat");

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        RecyclerView rvJadwal = findViewById(R.id.rv_jadwal);
        rvJadwal.setLayoutManager(new LinearLayoutManager(this));
        List<Map<String, Object>> dataList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        rvJadwal.setAdapter(new ReminderAdapter(this, dataList, index -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Apakah Anda Yakin?");
            builder.setMessage("Apakah Anda yakin sudah meminum obat ini?");
            builder.setPositiveButton("Sudah", (dialog, which) -> {
                db.collection("reminder")
                        .document(dataList.get(index).get("id").toString())
                        .update("sudah", true);

                dataList.remove(index);
                Objects.requireNonNull(rvJadwal.getAdapter()).notifyItemRemoved(index);
            });

            builder.setNegativeButton("Belum", (dialog, which) -> {
            });

            builder.show();
        }));

        db.collection("reminder")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (int i = 0; i < task.getResult().size(); i++) {
                            if (task.getResult().getDocuments().get(i).get("uid").equals(auth.getCurrentUser().getUid()) && !task.getResult().getDocuments().get(i).get("sudah").equals(true)) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("id", task.getResult().getDocuments().get(i).getId());
                                data.put("uid", task.getResult().getDocuments().get(i).get("uid"));
                                data.put("nama", task.getResult().getDocuments().get(i).get("nama"));
                                data.put("dosis", task.getResult().getDocuments().get(i).get("dosis"));
                                data.put("waktu", task.getResult().getDocuments().get(i).get("waktu"));
                                data.put("tanggal", task.getResult().getDocuments().get(i).get("tanggal"));
                                data.put("status", task.getResult().getDocuments().get(i).get("status"));
                                data.put("sudah", task.getResult().getDocuments().get(i).get("sudah"));

                                dataList.add(data);
                                rvJadwal.getAdapter().notifyDataSetChanged();
                            }
                        }
                    }
                });
    }
}