package com.example.passwordmanager;

import android.view.LayoutInflater;              // For inflating layout XML files
import android.view.View;                        // For UI elements
import android.view.ViewGroup;                   // For handling layout groups
import android.widget.TextView;                  // For displaying text

import androidx.annotation.NonNull;              // For nullability annotations
import androidx.recyclerview.widget.RecyclerView;// For scrolling lists

import java.util.List;                           // For handling collections of data

/**
 * This class is responsible for displaying the list of accounts in the RecyclerView.
 * It connects the data (Account objects) to the visual elements displayed in the list.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private final List<Account> accounts;        // Data source - all the accounts to display
    private final OnAccountClickListener listener;// Click listener to handle interactions

    /**
     * Interface for handling click events on account items
     * Implemented by MainActivity to receive these events
     */
    public interface OnAccountClickListener {
        void onAccountClick(Account account);             // Regular click - view account details
        void onAccountEdit(Account account, int position); // Edit action - modify account
        void onAccountDelete(Account account, int position); // Delete action - remove account
    }

    /**
     * Constructor for the adapter
     * @param accounts List of Account objects to display
     * @param listener Click event handler (MainActivity)
     */
    public AccountAdapter(List<Account> accounts, OnAccountClickListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder to represent an item
     * Creates and initializes the view holder for an account item
     */
    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single account item from XML
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    /**
     * Called to display the data at a specific position
     * Updates the contents of a ViewHolder to reflect the account at the given position
     */
    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        // Get the Account object at this position
        Account account = accounts.get(position);

        // Display the account website or service name
        holder.accountName.setText(account.getAccount());

        // Extract and display the initial letter from the domain name
        String domain = account.getAccount();
        String initial = "";
        if (domain != null && !domain.isEmpty()) {
            // Handle different domain formats to extract the first letter
            if (domain.contains(".")) {
                // Split by dot (e.g., "google.com" -> ["google", "com"])
                String[] parts = domain.split("\\.");
                if (parts.length >= 2) {
                    // Take first letter of the domain (e.g., "g" from "google.com")
                    initial = parts[0].substring(0, 1).toUpperCase();
                } else {
                    // If splitting didn't work as expected, just take the first letter
                    initial = domain.substring(0, 1).toUpperCase();
                }
            } else {
                // No dots, just take the first letter
                initial = domain.substring(0, 1).toUpperCase();
            }
        }
        holder.initialText.setText(initial);

        // Set up click listener for the whole item
        holder.itemView.setOnClickListener(v -> listener.onAccountClick(account));
    }

    /**
     * Returns the total number of accounts in the data set
     */
    @Override
    public int getItemCount() {
        return accounts.size();
    }

    /**
     * ViewHolder class that represents a single account item in the RecyclerView
     * Holds references to the UI elements in the item layout
     */
    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView accountName;  // Shows the account website/service name
        TextView initialText;  // Shows the first letter of the domain name in a circle

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views within the account item layout
            accountName = itemView.findViewById(R.id.account_name);
            initialText = itemView.findViewById(R.id.account_initial);
        }
    }
}