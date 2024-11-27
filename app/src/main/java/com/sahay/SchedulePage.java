package com.sahay;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SchedulePage extends AppCompatActivity {
    Button monBtn;
    Button tueBtn;
    Button wedBtn;
    Button thuBtn;
    Button friBtn;
    Button satBtn;
    Button sunBtn;
    Button verifyBtn2;
    ImageView backImageView;

    public static final String detailsKey="medicineKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent1=getIntent();

        monBtn=findViewById(R.id.monButton);
        tueBtn=findViewById(R.id.tueButton);
        wedBtn=findViewById(R.id.wedButton);
        thuBtn=findViewById(R.id.thuButton);
        friBtn=findViewById(R.id.friButton);
        satBtn=findViewById(R.id.satButton);
        sunBtn=findViewById(R.id.sunButton);
        verifyBtn2=findViewById(R.id.verifyButton2);
        backImageView=findViewById(R.id.backImageView);

        // Default on Tuesday
        setSelectedColorBackground(tueBtn);

        monBtn.setOnClickListener(v -> {
            setSelectedColorBackground(monBtn);
            clearButtons(tueBtn, wedBtn, thuBtn, friBtn, satBtn, sunBtn);
        });

        tueBtn.setOnClickListener(v -> {
            setSelectedColorBackground(tueBtn);
            clearButtons(monBtn, wedBtn, thuBtn, friBtn, satBtn, sunBtn);
        });

        wedBtn.setOnClickListener(v -> {
            setSelectedColorBackground(wedBtn);
            clearButtons(tueBtn, monBtn, thuBtn, friBtn, satBtn, sunBtn);
        });

        thuBtn.setOnClickListener(v -> {
            setSelectedColorBackground(thuBtn);
            clearButtons(tueBtn, wedBtn, monBtn, friBtn, satBtn, sunBtn);
        });

        friBtn.setOnClickListener(v -> {
            setSelectedColorBackground(friBtn);
            clearButtons(tueBtn, wedBtn, thuBtn, monBtn, satBtn, sunBtn);
        });

        satBtn.setOnClickListener(v -> {
            setSelectedColorBackground(satBtn);
            clearButtons(tueBtn, wedBtn, thuBtn, friBtn, monBtn, sunBtn);
        });

        sunBtn.setOnClickListener(v -> {
            setSelectedColorBackground(sunBtn);
            clearButtons(tueBtn, wedBtn, thuBtn, friBtn, satBtn, monBtn);
        });

        verifyBtn2.setOnClickListener(v -> {
            String find="PARACETAMOL";
            Intent intent=new Intent(SchedulePage.this, MainActivity.class);
            intent.putExtra(detailsKey, find);
            startActivity(intent);
        });

        backImageView.setOnClickListener(v -> {
            finish();
        });
    }

    private void clearButtons(Button a, Button b, Button c, Button d, Button e, Button f) {
        setWhiteBackground(a);
        setWhiteBackground(b);
        setWhiteBackground(c);
        setWhiteBackground(d);
        setWhiteBackground(e);
        setWhiteBackground(f);
    }

    private void setSelectedColorBackground(Button btn) {
        GradientDrawable drawableBtn = (GradientDrawable) btn.getBackground();
        int colorWhenSelected = ContextCompat.getColor(this, R.color.cyan);
        drawableBtn.setColor(colorWhenSelected);
    }

    private void setWhiteBackground(Button btn) {
        GradientDrawable drawableBtn = (GradientDrawable) btn.getBackground();
        int colorWhenSelected = ContextCompat.getColor(this, R.color.white);
        drawableBtn.setColor(colorWhenSelected);
    }
}