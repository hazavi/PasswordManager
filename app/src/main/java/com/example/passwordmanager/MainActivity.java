package com.example.passwordmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * MainActivity is the main screen of the app showing a list of saved accounts.
 * This class handles displaying accounts, swipe actions, and navigation to other screens.
 */
public class MainActivity extends AppCompatActivity implements AccountAdapter.OnAccountClickListener {

    // ArrayList to store all the user's password accounts
    private ArrayList<Account> accounts = new ArrayList<>();

    // UI elements we'll need to reference in our code
    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private TextView userInitial;
    private TextView userEmail;
    private View settingsTab;
    private View accountsTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout file for this activity
        setContentView(R.layout.activity_main);

        // Find and store references to UI elements from our layout
        recyclerView = findViewById(R.id.accountList);
        userInitial = findViewById(R.id.initial_circle);
        userEmail = findViewById(R.id.user_email);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        accountsTab = findViewById(R.id.accounts_tab);
        settingsTab = findViewById(R.id.settings_tab);

        // Configure the RecyclerView to display items in a vertical list
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Optimization when we know the size won't change

        // Load saved accounts from the device's SharedPreferences storage
        loadAccountsFromPreferences();

        // Create an adapter to display our accounts in the RecyclerView
        adapter = new AccountAdapter(accounts, this);
        recyclerView.setAdapter(adapter);

        // Add swipe functionality to RecyclerView items
        setupSwipeActions();

        // Set up mock user info (in a real app, this would come from a login system)
        userEmail.setText("passwordmanager@gmail.com");
        userInitial.setText("P"); // First letter of the user's name

        // When the "+" button is clicked, open the AddAccountActivity
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddAccountActivity.class);
            startActivity(intent);
        });

        // Set up bottom navigation bar clicks
        accountsTab.setOnClickListener(v -> {
            // We're already on accounts tab, just show a message
            Toast.makeText(this, "Accounts tab selected", Toast.LENGTH_SHORT).show();
        });

        settingsTab.setOnClickListener(v -> {
            Toast.makeText(this, "Settings tab selected", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Sets up swipe actions for RecyclerView items:
     * - Swipe left to delete an account
     * - Swipe right to edit an account
     */
    private void setupSwipeActions() {
        // Create a new ItemTouchHelper with a SimpleCallback
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            // Create background colors and paint for our swipe actions
            private final ColorDrawable editBackground = new ColorDrawable(Color.parseColor("#4F5B76"));
            private final ColorDrawable deleteBackground = new ColorDrawable(Color.parseColor("#FF5C5C"));
            private final Paint clearPaint = new Paint();

            // We're not implementing drag & drop functionality
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // Return false since we don't want drag & drop
            }

            // Handle the swipe action when it completes
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Account account = accounts.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    // User swiped left, show delete confirmation dialog
                    showDeleteDialog(account, position);
                } else {
                    // User swiped right, open edit screen
                    Intent intent = new Intent(MainActivity.this, EditAccountActivity.class);
                    intent.putExtra("id", account.getId());
                    intent.putExtra("account", account.getAccount());
                    intent.putExtra("name", account.getName());
                    intent.putExtra("password", account.getPassword());
                    startActivity(intent);

                    // Reset the item to its original state (cancel the swipe effect)
                    adapter.notifyItemChanged(position);
                }
            }

            // Draw the swipe action backgrounds and icons
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                if (dX > 0) {
                    // User is swiping to the right (edit action)
                    editBackground.setBounds(itemView.getLeft(),
                            itemView.getTop(),
                            itemView.getLeft() + (int)dX,
                            itemView.getBottom());
                    editBackground.draw(c);

                    // Draw edit icon
                    Drawable editIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_edit);
                    if (editIcon != null) {
                        int iconMargin = (itemView.getHeight() - editIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = itemView.getLeft() + iconMargin + editIcon.getIntrinsicWidth();
                        int iconBottom = iconTop + editIcon.getIntrinsicHeight();

                        editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        editIcon.setTint(Color.WHITE);
                        editIcon.draw(c);

                        // Draw "Edit" text
                        Paint textPaint = new Paint();
                        textPaint.setColor(Color.WHITE);
                        textPaint.setTextSize(36);
                        c.drawText("Edit", iconRight + iconMargin,
                                itemView.getTop() + itemView.getHeight() / 2f + 12, textPaint);
                    }
                } else if (dX < 0) {
                    // User is swiping to the left (delete action)
                    deleteBackground.setBounds(itemView.getRight() + (int)dX,
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom());
                    deleteBackground.draw(c);

                    // Draw delete icon
                    Drawable deleteIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.delete_24);
                    if (deleteIcon != null) {
                        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconRight = itemView.getRight() - iconMargin;
                        int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.setTint(Color.WHITE);
                        deleteIcon.draw(c);

                        // Draw "Delete" text
                        Paint textPaint = new Paint();
                        textPaint.setColor(Color.WHITE);
                        textPaint.setTextSize(36);
                        float textWidth = textPaint.measureText("Delete");
                        c.drawText("Delete", iconLeft - textWidth - iconMargin,
                                itemView.getTop() + itemView.getHeight() / 2f + 12, textPaint);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView); // Attach our helper to the RecyclerView
    }

    /**
     * Shows a confirmation dialog before deleting an account
     */
    private void showDeleteDialog(Account account, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Remove the item from our list and update the adapter
                    accounts.remove(position);
                    adapter.notifyItemRemoved(position);
                    rewritePreferences();

                    // Show a snackbar with undo option
                    Snackbar.make(recyclerView, "Account deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                // Undo deletion: add the item back
                                accounts.add(position, account);
                                adapter.notifyItemInserted(position);
                                rewritePreferences();
                            })
                            .show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled the deletion, reset the item's appearance
                    adapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {
                    // If dialog is dismissed by tapping outside, also reset the item
                    adapter.notifyItemChanged(position);
                })
                .show();
    }

    // Implement the click listener methods from our interface

    /**
     * Called when user taps on an account in the list
     */
    @Override
    public void onAccountClick(Account account) {
        // Open the account details screen
        Intent intent = new Intent(MainActivity.this, AccountDetailActivity.class);
        intent.putExtra("id", account.getId());
        intent.putExtra("account", account.getAccount());
        intent.putExtra("name", account.getName());
        intent.putExtra("password", account.getPassword());
        startActivity(intent);
    }

    /**
     * Called when user wants to edit an account
     */
    @Override
    public void onAccountEdit(Account account, int position) {
        // Open the edit screen
        Intent intent = new Intent(MainActivity.this, EditAccountActivity.class);
        intent.putExtra("id", account.getId());
        intent.putExtra("account", account.getAccount());
        intent.putExtra("name", account.getName());
        intent.putExtra("password", account.getPassword());
        startActivity(intent);
    }

    /**
     * Called when user wants to delete an account
     */
    @Override
    public void onAccountDelete(Account account, int position) {
        showDeleteDialog(account, position);
    }

    /**
     * Load saved accounts from SharedPreferences storage
     */
    private void loadAccountsFromPreferences() {
        accounts.clear(); // Clear existing list
        SharedPreferences prefs = getSharedPreferences("accounts", MODE_PRIVATE);
        String allData = prefs.getString("data", "");

        if (allData != null && !allData.isEmpty()) {
            // Split the stored data by newline to get each account
            String[] lines = allData.split("\n");
            for (String line : lines) {
                if (line.isEmpty()) continue;

                // Parse each account's data
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    // Format: id,account,name,password
                    accounts.add(new Account(parts[0], parts[1], parts[2], parts[3]));
                } else if (parts.length == 3) {
                    // Support older format: account,name,password (no ID)
                    String id = System.currentTimeMillis() + "_" + parts[0];
                    accounts.add(new Account(id, parts[0], parts[1], parts[2]));
                }
            }
        }
    }

    /**
     * Save the current list of accounts to SharedPreferences
     */
    private void rewritePreferences() {
        SharedPreferences prefs = getSharedPreferences("accounts", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Build a string with all account data
        StringBuilder builder = new StringBuilder();
        for (Account acc : accounts) {
            builder.append(acc.getId()).append(",")
                    .append(acc.getAccount()).append(",")
                    .append(acc.getName()).append(",")
                    .append(acc.getPassword()).append("\n");
        }

        // Save the string to preferences
        editor.putString("data", builder.toString());
        editor.apply();
    }

    /**
     * Called when the activity comes back to the foreground
     * Refreshes the account list in case anything changed (like after editing)
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadAccountsFromPreferences();
        adapter.notifyDataSetChanged();
    }
}