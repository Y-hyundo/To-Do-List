package com.cookandroid.miniproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;

public class SelectDay extends AppCompatActivity {
    Button btnSelect, btnBack;
    DatePicker datePciker;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_day);

        btnSelect = (Button) findViewById(R.id.BtnSelect);
        btnBack = (Button) findViewById(R.id.BtnBack);
        datePciker = (DatePicker) findViewById(R.id.DatePicker);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDate = getCurrentDate();
                Intent intent = new Intent(getApplicationContext(), TaskListActivity.class);
                intent.putExtra("selectedDate", selectedDate);
                startActivity(intent);
            }
        });
    }

    private String getCurrentDate() {
        int year = datePciker.getYear();
        int month = datePciker.getMonth() + 1;
        int day = datePciker.getDayOfMonth();

        return Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);
    }
}
