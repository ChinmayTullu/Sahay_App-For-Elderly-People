package com.sahay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LandingPage extends AppCompatActivity {
    LinearLayout schedule;
    LinearLayout hire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        schedule=findViewById(R.id.scheduleLayout);
        hire=findViewById(R.id.hireLayout);

        schedule.setOnClickListener(v -> {
            Intent intent=new Intent(LandingPage.this, SchedulePage.class);
            startActivity(intent);
        });

        hire.setOnClickListener(v -> {
            Intent intent1=new Intent(LandingPage.this, MarketplaceSecond.class);
            startActivity(intent1);
        });
    }
}