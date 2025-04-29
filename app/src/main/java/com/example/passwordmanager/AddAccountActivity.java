package com.example.passwordmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;

public class AddAccountActivity extends AppCompatActivity {

    private AutoCompleteTextView nameInput;
    private EditText passwordInput;
    private final String[] suggestions = {"google.com", "facebook.com", "youtube.com", "spotify.com", "instagram.com", "gmail.com"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        nameInput = findViewById(R.id.input_name);
        passwordInput = findViewById(R.id.input_password);
        Button addBtn = findViewById(R.id.btn_add);

        // Autocomplete for name input
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        nameInput.setAdapter(adapter);
        nameInput.setThreshold(1);

        addBtn.setOnClickListener(view -> {
            String name = nameInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (!name.isEmpty() && !password.isEmpty()) {
                saveAccountToFile(name, password);
                Toast.makeText(this, "Account saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAccountToFile(String name, String password) {
        try (FileOutputStream fos = openFileOutput("accounts.txt", MODE_APPEND)) {
            fos.write((name + "," + password + "\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }

}
