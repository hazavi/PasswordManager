package com.example.passwordmanager;
/**
 * This class represents a single password account stored in the password manager.
 * It contains all the essential information needed to identify and authenticate
 * with an online service or website.
 */

public class Account {
    private String id;           // Unique identifier for the account in our database
    private final String account; // The website or service name (e.g. google.com)
    private final String name;    // The username or email used to log in
    private final String password; // The password for the account (stored in plain text for simplicity)

    /**
     * Constructor that automatically generates a unique ID
     * Used when creating a brand new account
     *
     * @param account  Website or service name (e.g. google.com)
     * @param name     Username or email used for this account
     * @param password Password for this account
     */
    public Account(String account, String name, String password) {
        // Create a unique ID using current timestamp plus account name
        // This helps ensure uniqueness even if multiple accounts have the same website name
        this.id = System.currentTimeMillis() + "_" + account;
        this.account = account;
        this.name = name;
        this.password = password;
    }

    /**
     * Constructor with explicit ID parameter
     * Used when loading existing accounts from storage where the ID is already known
     *
     * @param id       Unique identifier for the account
     * @param account  Website or service name (e.g. google.com)
     * @param name     Username or email used for this account
     * @param password Password for this account
     */
    public Account(String id, String account, String name, String password) {
        this.id = id;
        this.account = account;
        this.name = name;
        this.password = password;
    }

    /**
     * Gets the unique identifier for this account
     * @return The account's ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the website or service name
     * @return The account's website or service name (e.g. google.com)
     */
    public String getAccount() {
        return account;
    }

    /**
     * Gets the username or email address
     * @return The account's username or email
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the password
     * @return The account's password
     */
    public String getPassword() {
        return password;
    }
}