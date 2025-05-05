package com.example.passwordmanager;

import android.os.Bundle;                       // For activity lifecycle management
import android.text.InputType;                  // For controlling the input type of EditText fields
import android.text.TextUtils;                  // For text validation
import android.view.View;                       // For UI interactions
import android.widget.Button;                   // For clickable buttons
import android.widget.EditText;                 // For text input fields
import android.widget.ImageButton;              // For image-based buttons
import android.widget.Toast;                    // For showing temporary messages
import androidx.appcompat.app.AlertDialog;      // For showing dialog boxes
import androidx.appcompat.app.AppCompatActivity; // Base class for activities
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Material design FAB
import android.content.SharedPreferences;       // For storing account data locally
import java.util.ArrayList;                     // For manipulating collections of objects

/**
 * This class handles editing an existing account's details.
 * It allows users to update the account name, username, and password,
 * and validates input before saving changes.
 */
public class EditAccountActivity extends AppCompatActivity {

    // UI element references that we'll need to interact with programmatically
    private EditText etAccount, etUsername, etPassword;  // Input fields
    private ImageButton btnTogglePassword;               // Password visibility toggle button
    private FloatingActionButton fabMenu;                // Menu button (if implemented)
    private String accountId;                            // ID of the account being edited
    private boolean passwordVisible = false;             // Track if password is currently visible

    /**
     * Called when the activity is first created.
     * Sets up the UI and loads the account details for editing.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        // Find and store references to UI elements from the layout
        etAccount = findViewById(R.id.et_account);     // Account/website input field
        etUsername = findViewById(R.id.et_username);   // Username input field
        etPassword = findViewById(R.id.et_password);   // Password input field
        ImageButton btnBack = findViewById(R.id.btn_back); // Back navigation button
        Button btnSave = findViewById(R.id.btn_save);  // Save changes button
        btnTogglePassword = findViewById(R.id.btn_toggle_password); // Password visibility toggle

        // Get the account details passed from the previous screen
        accountId = getIntent().getStringExtra("id");
        String account = getIntent().getStringExtra("account");
        String username = getIntent().getStringExtra("name");
        String password = getIntent().getStringExtra("password");

        // Fill the form fields with current account data
        etAccount.setText(account);
        etUsername.setText(username);
        etPassword.setText(password);

        // Set up click listener for back button
        btnBack.setOnClickListener(v -> {
            // Check if user has made any changes before going back
            if (hasChanges(account, username, password)) {
                // If changes were made, show a confirmation dialog
                showUnsavedChangesDialog();
            } else {
                // No changes, just go back
                finish();
            }
        });

        // Set up click listener for save button
        btnSave.setOnClickListener(v -> {
            // Validate inputs before saving
            if (validateInputs()) {
                // If all inputs are valid, save the changes
                saveChanges();
            }
        });

        // Set up password visibility toggle
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    /**
     * Checks if any of the account details have been changed
     * @param originalAccount Original account name
     * @param originalUsername Original username
     * @param originalPassword Original password
     * @return true if any field has changed, false if all unchanged
     */
    private boolean hasChanges(String originalAccount, String originalUsername, String originalPassword) {
        return !originalAccount.equals(etAccount.getText().toString()) ||
                !originalUsername.equals(etUsername.getText().toString()) ||
                !originalPassword.equals(etPassword.getText().toString());
    }

    /**
     * Shows a dialog asking if the user wants to discard unsaved changes
     */
    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Do you want to discard them?")
                .setPositiveButton("Discard", (dialog, which) -> finish()) // If yes, exit without saving
                .setNegativeButton("Keep Editing", null)  // If no, dismiss dialog and continue editing
                .show();
    }

    /**
     * Toggles the visibility of the password text between plaintext and masked dots
     */
    private void togglePasswordVisibility() {
        if (passwordVisible) {
            // Password is currently visible, hide it (show dots)
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility); // Show "show password" icon
        } else {
            // Password is currently hidden, show it as plaintext
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off); // Show "hide password" icon
        }
        passwordVisible = !passwordVisible; // Toggle the state flag

        // Move cursor to end of text (prevents cursor jumping to start)
        etPassword.setSelection(etPassword.getText().length());
    }

    /**
     * Validates that all required fields contain data
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Check if the account name field is empty
        if (TextUtils.isEmpty(etAccount.getText())) {
            etAccount.setError("Account name required");
            isValid = false;
        }

        // Check if the username field is empty
        if (TextUtils.isEmpty(etUsername.getText())) {
            etUsername.setError("Username required");
            isValid = false;
        }

        // Check if the password field is empty
        if (TextUtils.isEmpty(etPassword.getText())) {
            etPassword.setError("Password required");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Saves the updated account details to SharedPreferences storage
     */
    private void saveChanges() {
        // Access the app's SharedPreferences storage
        SharedPreferences prefs = getSharedPreferences("accounts", MODE_PRIVATE);
        String allData = prefs.getString("data", "");
        ArrayList<Account> accounts = new ArrayList<>();

        // Process all existing accounts in the storage
        if (allData != null && !allData.isEmpty()) {
            String[] lines = allData.split("\n");
            for (String line : lines) {
                if (line.isEmpty()) continue; // Skip empty lines

                String[] parts = line.split(",");
                if (parts.length == 4) {
                    if (parts[0].equals(accountId)) {
                        // This is the account we're editing - add the updated version
                        accounts.add(new Account(
                                accountId,
                                etAccount.getText().toString(),
                                etUsername.getText().toString(),
                                etPassword.getText().toString()
                        ));
                    } else {
                        // This is not the account we're editing - keep it as is
                        accounts.add(new Account(parts[0], parts[1], parts[2], parts[3]));
                    }
                } else if (parts.length == 3) {
                    // Support for older format without ID field
                    String id = System.currentTimeMillis() + "_" + parts[0];
                    accounts.add(new Account(id, parts[0], parts[1], parts[2]));
                }
            }
        }

        // Rewrite all accounts to SharedPreferences with the updated data
        SharedPreferences.Editor editor = prefs.edit();
        StringBuilder builder = new StringBuilder();
        for (Account acc : accounts) {
            builder.append(acc.getId()).append(",")
                    .append(acc.getAccount()).append(",")
                    .append(acc.getName()).append(",")
                    .append(acc.getPassword()).append("\n");
        }

        editor.putString("data", builder.toString());
        editor.apply();

        // Show confirmation to the user
        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();

        // Return to previous screen
        finish();
    }

    /**
     * Handles the back button press (hardware or system back button)
     * Shows confirmation dialog if there are unsaved changes
     */
    @Override
    public void onBackPressed() {
        // Check if there are unsaved changes
        if (hasChanges(getIntent().getStringExtra("account"),
                getIntent().getStringExtra("name"),
                getIntent().getStringExtra("password"))) {
            // Show confirmation dialog for unsaved changes
            showUnsavedChangesDialog();
        } else {
            // No changes, proceed with normal back button behavior
            super.onBackPressed();
        }
    }
}