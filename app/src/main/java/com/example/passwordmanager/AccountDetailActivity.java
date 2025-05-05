package com.example.passwordmanager;

// Import required libraries and classes
import android.content.ClipData;                // For copying to clipboard
import android.content.ClipboardManager;        // For clipboard management
import android.content.Context;                 // For accessing system services
import android.content.DialogInterface;         // For handling dialog button clicks
import android.content.Intent;                  // For navigating between activities
import android.content.SharedPreferences;       // For storing account data locally
import android.os.Bundle;                       // For activity lifecycle management
import android.view.View;                       // For UI interactions
import android.widget.ImageButton;              // For interactive image buttons
import android.widget.TextView;                 // For displaying text content
import android.widget.Toast;                    // For showing temporary messages

// Android support libraries
import androidx.appcompat.app.AlertDialog;      // For showing dialog boxes
import androidx.appcompat.app.AppCompatActivity; // Base class for activities

// Material design components
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;                     // For manipulating collections of objects

/**
 * This class handles displaying the details of a specific saved account.
 * Users can view account information, copy the password, toggle password visibility,
 * edit the account details, or delete the account.
 */
public class AccountDetailActivity extends AppCompatActivity {

    // UI element references that we'll need to interact with programmatically
    private TextView tvAccount, tvName, tvPassword;                  // Text display fields
    private ImageButton btnBack, btnTogglePassword, btnCopyPassword; // Action buttons
    private FloatingActionButton fabEdit, fabDelete;                 // Floating action buttons
    private boolean isPasswordVisible = false;                       // Track password visibility state
    private String accountId;                                        // Unique identifier for this account

    /**
     * Called when the activity is first created.
     * Sets up the UI and initializes the account details.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout from activity_account_detail.xml
        setContentView(R.layout.activity_account_detail);

        // Find and store references to UI elements from the layout
        tvAccount = findViewById(R.id.tv_account);           // Website/service name
        tvName = findViewById(R.id.tv_name);                 // Username
        tvPassword = findViewById(R.id.tv_password);         // Password field

        btnBack = findViewById(R.id.btn_back);               // Back navigation button
        btnTogglePassword = findViewById(R.id.btn_toggle_password);  // Show/hide password button
        btnCopyPassword = findViewById(R.id.btn_copy_password);      // Copy password button

        fabEdit = findViewById(R.id.fab_edit);               // Edit account button
        fabDelete = findViewById(R.id.fab_delete);           // Delete account button

        // Retrieve the account details passed from the previous screen
        accountId = getIntent().getStringExtra("id");        // Unique identifier
        String accountSite = getIntent().getStringExtra("account");  // Website/service name
        String accountName = getIntent().getStringExtra("name");     // Username
        String accountPassword = getIntent().getStringExtra("password"); // Actual password

        // Display the account details in the UI
        tvAccount.setText(accountSite);
        tvName.setText(accountName);

        // For security, passwords are initially masked with bullets
        maskPassword(accountPassword);

        // Set up all the click actions for the buttons
        setupClickListeners();
    }

    /**
     * Configures the click actions for all buttons in the UI
     */
    private void setupClickListeners() {
        // Back button - return to the previous screen when clicked
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close this activity and return to previous one
            }
        });

        // Toggle password visibility button - show or hide the actual password
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        // Copy password button - copy password to clipboard
        btnCopyPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyPasswordToClipboard();
            }
        });

        // Edit button - open the edit screen with this account's details
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditScreen();
            }
        });

        // Delete button - show confirmation dialog then delete if confirmed
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmation();
            }
        });
    }

    /**
     * Toggles between showing the actual password text and showing masked bullets
     */
    private void togglePasswordVisibility() {
        // Get the actual password from the intent extras
        String actualPassword = getIntent().getStringExtra("password");

        if (isPasswordVisible) {
            // Password is currently visible, so hide it
            maskPassword(actualPassword);
            // Update the icon to show the "show password" icon
            btnTogglePassword.setImageResource(R.drawable.ic_visibility);
        } else {
            // Password is currently hidden, so show it
            tvPassword.setText(actualPassword);
            // Update the icon to show the "hide password" icon
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        }

        // Toggle the state flag
        isPasswordVisible = !isPasswordVisible;
    }

    /**
     * Replaces a password string with bullet characters (•) for secure display
     * @param password The actual password to mask
     */
    private void maskPassword(String password) {
        // Handle empty passwords
        if (password == null || password.isEmpty()) {
            tvPassword.setText("");
            return;
        }

        // Create a string of bullet characters with the same length as the password
        StringBuilder maskedPassword = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            maskedPassword.append("•"); // Bullet character for each character in password
        }
        tvPassword.setText(maskedPassword.toString());
    }

    /**
     * Copies the account password to the system clipboard
     */
    private void copyPasswordToClipboard() {
        // Get the password from intent extras
        String password = getIntent().getStringExtra("password");

        // Get the system clipboard service
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // Create a new clip containing the password
        ClipData clip = ClipData.newPlainText("Password", password);
        // Copy the password to clipboard
        clipboard.setPrimaryClip(clip);

        // Show feedback so the user knows the action was successful
        Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens the EditAccountActivity to modify this account's details
     */
    private void openEditScreen() {
        // Create an intent to navigate to the edit screen
        Intent intent = new Intent(this, EditAccountActivity.class);

        // Pass all the current account data to the edit screen
        intent.putExtra("id", accountId);
        intent.putExtra("account", tvAccount.getText().toString());
        intent.putExtra("name", tvName.getText().toString());
        intent.putExtra("password", getIntent().getStringExtra("password"));

        // Navigate to the edit screen
        startActivity(intent);
    }

    /**
     * Shows a confirmation dialog before deleting an account
     */
    private void showDeleteConfirmation() {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete this account?");

        // Add the confirm button
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User confirmed deletion, so delete the account
                deleteAccount();
            }
        });

        // Add the cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User canceled, just dismiss the dialog
                dialog.dismiss();
            }
        });

        // Create and display the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Deletes the current account from SharedPreferences storage
     */
    private void deleteAccount() {
        // Get access to the app's SharedPreferences storage
        SharedPreferences prefs = getSharedPreferences("accounts", MODE_PRIVATE);
        String allData = prefs.getString("data", "");
        ArrayList<Account> accounts = new ArrayList<>();

        // Process all existing accounts, excluding the one to delete
        if (allData != null && !allData.isEmpty()) {
            String[] lines = allData.split("\n");
            for (String line : lines) {
                if (line.isEmpty()) continue; // Skip empty lines

                String[] parts = line.split(",");
                if (parts.length == 4 && !parts[0].equals(accountId)) {
                    // This is not the account we're deleting, so keep it
                    accounts.add(new Account(parts[0], parts[1], parts[2], parts[3]));
                } else if (parts.length == 3) {
                    // Support for older format without ID
                    String id = System.currentTimeMillis() + "_" + parts[0];
                    accounts.add(new Account(id, parts[0], parts[1], parts[2]));
                }
            }
        }

        // Rewrite the SharedPreferences without the deleted account
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
        Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();

        // Close this screen and return to the list
        finish();
    }

    /**
     * Called when the activity comes back to the foreground
     * Updates the UI in case the account was edited
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Check if account was edited by looking it up from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("accounts", MODE_PRIVATE);
        String allData = prefs.getString("data", "");

        if (allData != null && !allData.isEmpty() && accountId != null) {
            String[] lines = allData.split("\n");
            for (String line : lines) {
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length == 4 && parts[0].equals(accountId)) {
                    // Found the account with matching ID, update the UI
                    tvAccount.setText(parts[1]);
                    tvName.setText(parts[2]);
                    maskPassword(parts[3]);

                    // Update intent extras to keep them in sync with current data
                    getIntent().putExtra("account", parts[1]);
                    getIntent().putExtra("name", parts[2]);
                    getIntent().putExtra("password", parts[3]);
                    break;
                }
            }
        }
    }
}