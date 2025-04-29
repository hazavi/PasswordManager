package com.example.passwordmanager;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AccountDetailActivity extends AppCompatActivity {

    private TextView tvAccountName, tvAccountPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        tvAccountName = findViewById(R.id.tv_account_name);
        tvAccountPassword = findViewById(R.id.tv_account_password);

        // Correct keys
        String accountName = getIntent().getStringExtra("name");
        String accountPassword = getIntent().getStringExtra("password");

        tvAccountName.setText("Name: " + accountName);
        tvAccountPassword.setText("Password: " + accountPassword);
    }

}
