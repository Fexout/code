package com.ingat.obat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().hide();

        FirebaseAuth auth = FirebaseAuth.getInstance();

        TextInputLayout tilNama = findViewById(R.id.til_nama);
        TextInputLayout tilEmail = findViewById(R.id.til_email);
        TextInputLayout tilPassword = findViewById(R.id.til_password);

        TextInputEditText etEmail = findViewById(R.id.et_email);
        TextInputEditText etPassword = findViewById(R.id.et_password);
        TextInputEditText etNama = findViewById(R.id.et_nama);

        etNama.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilNama.setError(null);
            }
        });

        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilEmail.setError(null);
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilPassword.setError(null);
            }
        });

        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            String email = String.valueOf(etEmail.getText());
            String password = String.valueOf(etPassword.getText());
            String nama = String.valueOf(etNama.getText());

            if (email.isEmpty())
                tilEmail.setError("Email tidak boleh kosong");

            if (password.isEmpty())
                tilPassword.setError("Password tidak boleh kosong");

            if (nama.isEmpty())
                tilNama.setError("Nama tidak boleh kosong");

            if (email.isEmpty() || password.isEmpty() || nama.isEmpty())
                return;

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userId = auth.getCurrentUser().getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("nama", nama);
                    userData.put("email", email);

                    db.collection("users").document(userId).set(userData).addOnSuccessListener(aVoid -> {
                        Toast.makeText(RegisterActivity.this, "Register berhasil", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(RegisterActivity.this, "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    tilEmail.setError("Email sudah terdaftar");
                }
            });
        });

        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}