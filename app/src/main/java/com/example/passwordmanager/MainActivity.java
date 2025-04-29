package com.example.passwordmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity {

    private ArrayList<Account> accounts = new ArrayList<>();
    private RecyclerView recyclerView;
    private AccountAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.accountList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAccountsFromFile();

        adapter = new AccountAdapter(accounts, account -> {
            Intent intent = new Intent(MainActivity.this, AccountDetailActivity.class);
            intent.putExtra("name", account.getName());
            intent.putExtra("password", account.getPassword());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Floating button to go to add screen
        findViewById(R.id.fab_add).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddAccountActivity.class);
            startActivity(intent);
        });

        // Swipe left to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                accounts.remove(pos);
                adapter.notifyItemRemoved(pos);
                rewriteFile();
                Toast.makeText(MainActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void loadAccountsFromFile() {
        accounts.clear();
        try {
            File file = new File(getFilesDir(), "accounts.txt");
            if (!file.exists()) return;

            FileInputStream fis = openFileInput("accounts.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ",");
                if (st.countTokens() == 2) {
                    String name = st.nextToken();
                    String password = st.nextToken();
                    accounts.add(new Account(name, password));
                }
            }
            reader.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rewriteFile() {
        try {
            deleteFile("accounts.txt");
            for (Account acc : accounts) {
                FileOutputStream fos = openFileOutput("accounts.txt", MODE_APPEND);
                fos.write((acc.getName() + "," + acc.getPassword() + "\n").getBytes());
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccountsFromFile();
        adapter.notifyDataSetChanged();
    }
}