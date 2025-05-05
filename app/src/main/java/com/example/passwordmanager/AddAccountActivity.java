package com.example.passwordmanager;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import java.util.Random;

/**
 * AddAccountActivity allows users to add new password accounts.
 * Features include autocomplete suggestions for common websites,
 * password visibility toggle, and a secure password generator.
 */
public class AddAccountActivity extends AppCompatActivity {

    // UI elements we'll need to reference in our code
    private AutoCompleteTextView accountInput;
    private EditText nameInput, passwordInput;
    private ImageButton btnBack, btnGeneratePassword, btnTogglePassword;
    private Button btnAdd;
    private boolean isPasswordVisible = false;

    // List of common website suggestions for the autocomplete feature
    private final String[] suggestions = {
            "google.com",
            "facebook.com",
            "youtube.com",
            "spotify.com",
            "instagram.com",
            "gmail.com",
            "tiktok.com",
            "twitch.com",
            "github.com",
            "pinterest.com",
            "discord.com",
            "netflix.com",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        // Find and store references to UI elements from our layout
        accountInput = findViewById(R.id.input_account);
        nameInput = findViewById(R.id.input_name);
        passwordInput = findViewById(R.id.input_password);
        btnBack = findViewById(R.id.btn_back);
        btnGeneratePassword = findViewById(R.id.btn_generate_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnAdd = findViewById(R.id.btn_add);

        // Set up back button to close this screen
        btnBack.setOnClickListener(v -> finish());

        // Set up autocomplete adapter with our website suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, suggestions);
        accountInput.setAdapter(adapter);
        accountInput.setThreshold(1); // Show suggestions after typing 1 character

        // Set up password visibility toggle button
        btnTogglePassword.setOnClickListener(v -> {
            togglePasswordVisibility();
        });

        // Set up password generator button
        btnGeneratePassword.setOnClickListener(v -> {
            // Generate a secure random password
            String generatedPassword = generateSecurePassword();
            passwordInput.setText(generatedPassword);

            // Show the password so user can see what was generated
            if (!isPasswordVisible) {
                togglePasswordVisibility();
            }

            // Show confirmation message
            Toast.makeText(this, "Password generated!", Toast.LENGTH_SHORT).show();
        });

        // Set up save button
        btnAdd.setOnClickListener(view -> {
            // Get user input values
            String account = accountInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Validate inputs
            if (account.isEmpty()) {
                accountInput.setError("Account name is required");
                return;
            }

            if (name.isEmpty()) {
                nameInput.setError("Username is required");
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                return;
            }

            // Save the new account
            saveAccountToPreferences(account, name, password);
            Toast.makeText(this, "Account saved successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        });
    }

    /**
     * Toggles password visibility between visible and hidden
     */
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password - show dots instead of characters
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility);
        } else {
            // Show password - display actual characters
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        }

        // Keep cursor at the end of text (prevents cursor jumping to start)
        passwordInput.setSelection(passwordInput.getText().length());

        isPasswordVisible = !isPasswordVisible;
    }

    /**
     * Generates a secure random password with mixed characters
     * Includes uppercase, lowercase, numbers, and special characters
     */
    private String generateSecurePassword() {
        // Define character sets for our password
        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        String allChars = upperChars + lowerChars + numbers + specialChars;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        // Start with one character from each type to ensure password complexity
        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Add additional random characters to reach desired length
        for (int i = 0; i < 8; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password to make it more random
        // (otherwise it would always start with uppercase, then lowercase, etc.)
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            // Swap characters at positions i and j
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    /**
     * Saves a new account to SharedPreferences storage
     */
    private void saveAccountToPreferences(String account, String name, String password) {
        SharedPreferences prefs = getSharedPreferences("accounts", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Generate a unique ID for this account
        String id = System.currentTimeMillis() + "_" + account;

        // Get existing accounts data
        String existing = prefs.getString("data", "");

        // Add the new account to the existing data
        String updated = existing + id + "," + account + "," + name + "," + password + "\n";

        // Save back to SharedPreferences
        editor.putString("data", updated);
        editor.apply();
    }
}