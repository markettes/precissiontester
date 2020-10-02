package com.marcostfg.precision_tester;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BlePreActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preactivity_ble);

        Button fiveTest = findViewById(R.id.fiveTest);
        Button tenTest = findViewById(R.id.tenTest);

        fiveTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BlePreActivity.this, BleActivity.class);
                i.putExtra("seconds", 5);
                startActivity(i);
            }
        });

        tenTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BlePreActivity.this, BleActivity.class);
                i.putExtra("seconds", 10);
                startActivity(i);
            }
        });
    }
}
