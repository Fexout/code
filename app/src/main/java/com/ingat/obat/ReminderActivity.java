package com.ingat.obat;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ReminderActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SCHEDULE_ALARM = 1;

    @SuppressLint("ScheduleExactAlarm")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_reminder);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().setTitle("Reminder Minum Obat");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        String[] statusArr = new String[3];
        statusArr[0] = "Sebelum Makan";
        statusArr[1] = "Sesudah Makan";
        statusArr[2] = "Ketika Makan";
        @SuppressLint("WrongViewCast") MaterialAutoCompleteTextView etStatus = findViewById(R.id.et_status);
        etStatus.setSimpleItems(statusArr);

        MaterialTimePicker waktuPicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Pilih Waktu Minum Obat")
                .build();

        var refWaktu = new Object() {
            String waktu = "";
        };
        waktuPicker.addOnPositiveButtonClickListener(v -> {
            String jam = waktuPicker.getHour() < 10 ? "0" + waktuPicker.getHour() : String.valueOf(waktuPicker.getHour());
            String menit = waktuPicker.getMinute() < 10 ? "0" + waktuPicker.getMinute() : String.valueOf(waktuPicker.getMinute());
            refWaktu.waktu = jam + ":" + menit;
        });

        TextView tvWaktu = findViewById(R.id.tv_waktu);
        Button btnWaktu = findViewById(R.id.btn_waktu);
        btnWaktu.setOnClickListener(v -> {
            tvWaktu.setHeight(0);
            tvWaktu.setText(null);
            waktuPicker.show(getSupportFragmentManager(), "waktu");
        });

        MaterialDatePicker<Long> tanggalPicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal Minum Obat")
                .build();

        var refTanggal = new Object() {
            String tanggal = "";
        };
        tanggalPicker.addOnPositiveButtonClickListener(v -> {
            refTanggal.tanggal = tanggalPicker.getHeaderText();
        });

        TextView tvTanggal = findViewById(R.id.tv_tanggal);
        Button btnTanggal = findViewById(R.id.btn_tanggal);
        btnTanggal.setOnClickListener(v -> {
            tvTanggal.setHeight(0);
            tvTanggal.setText(null);
            tanggalPicker.show(getSupportFragmentManager(), "tanggal");
        });

        TextInputLayout tilNama = findViewById(R.id.til_nama);
        TextInputLayout tilDosis = findViewById(R.id.til_dosis);
        TextInputLayout tilStatus = findViewById(R.id.til_status);

        TextInputEditText etNama = findViewById(R.id.et_nama);
        TextInputEditText etDosis = findViewById(R.id.et_dosis);

        etNama.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilNama.setError(null);
            }
        });

        etDosis.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilDosis.setError(null);
            }
        });

        etStatus.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilStatus.setError(null);
            }
        });

        Button btnSimpan = findViewById(R.id.btn_simpan);
        btnSimpan.setOnClickListener(v -> {
            boolean valid = true;
            String nama = etNama.getText().toString();
            String dosis = etDosis.getText().toString();
            String status = etStatus.getText().toString();

            if (nama.isEmpty()) {
                tilNama.setError("Nama obat harus diisi");
                valid = false;
            }

            if (dosis.isEmpty()) {
                tilDosis.setError("Dosis obat harus diisi");
                valid = false;
            }

            if (status.isEmpty()) {
                tilStatus.setError("Status obat harus diisi");
                valid = false;
            }

            if (refWaktu.waktu.isEmpty()) {
                tvWaktu.setError("Waktu obat harus diisi");
                valid = false;
            }

            if (refTanggal.tanggal.isEmpty()) {
                tvTanggal.setError("Tanggal obat harus diisi");
                valid = false;
            }

            if (valid) {
                Map<String, Object> reminder = new HashMap<>();
                reminder.put("uid", auth.getCurrentUser().getUid());
                reminder.put("nama", nama);
                reminder.put("dosis", dosis);
                reminder.put("status", status);
                reminder.put("waktu", refWaktu.waktu);
                reminder.put("tanggal", refTanggal.tanggal);
                reminder.put("sudah", false);

                db.collection("reminder")
                        .add(reminder)
                        .addOnSuccessListener(documentReference -> {
                            long selectedDateMillis = tanggalPicker.getSelection();

                            int hour = waktuPicker.getHour();
                            int minute = waktuPicker.getMinute();

                            Data inputData = new Data.Builder()
                                    .putString("message", "Saatnya minum obat " + nama + " , dosis " + dosis + " " + status)
                                    .build();

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(selectedDateMillis);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);

                            if (calendar.before(Calendar.getInstance())) {
                                calendar.add(Calendar.DAY_OF_YEAR, 1);
                            }

                            long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

                            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                                    .setInputData(inputData)
                                    .build();

                            WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);

                            Toast toast = Toast.makeText(ReminderActivity.this, "Reminder berhasil ditambahkan", Toast.LENGTH_LONG);
                            toast.show();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                toast.addCallback(new Toast.Callback() {
                                    @Override
                                    public void onToastHidden() {
                                        super.onToastHidden();
                                        startActivity(new Intent(ReminderActivity.this, MainActivity.class));
                                        finish();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(ReminderActivity.this, "Reminder gagal ditambahkan", Toast.LENGTH_LONG).show());
            }
        });
    }
}