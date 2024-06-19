package com.cookandroid.miniproject;

// 필요한 라이브러리와 클래스들을 임포트합니다.
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;

// SelectDay 클래스는 날짜를 선택하는 화면을 담당합니다.
public class SelectDay extends AppCompatActivity {
    // 버튼과 날짜 선택기 변수를 선언합니다.
    Button btnSelect, btnBack;
    DatePicker datePciker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Activity가 생성될 때 호출됩니다.
        super.onCreate(savedInstanceState);
        // select_day 레이아웃을 이 Activity의 사용자 인터페이스로 설정합니다.
        setContentView(R.layout.select_day);

        // select_day 레이아웃에서 BtnSelect, BtnBack, DatePicker의 ID를 가진 뷰를 찾아 변수에 할당합니다.
        btnSelect = (Button) findViewById(R.id.BtnSelect);
        btnBack = (Button) findViewById(R.id.BtnBack);
        datePciker = (DatePicker) findViewById(R.id.DatePicker);

        // btnBack 버튼에 클릭 리스너를 설정합니다.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼이 클릭되면 현재 Activity를 종료합니다.
                finish();
            }
        });

        // btnSelect 버튼에 클릭 리스너를 설정합니다.
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼이 클릭되면 현재 선택된 날짜를 가져옵니다.
                String selectedDate = getCurrentDate();
                // TaskListActivity를 시작하는 인텐트를 생성하고 선택된 날짜를 전달합니다.
                Intent intent = new Intent(getApplicationContext(), TaskListActivity.class);
                intent.putExtra("selectedDate", selectedDate);
                startActivity(intent);
            }
        });
    }

    // 현재 DatePicker에서 선택된 날짜를 "yyyy-MM-dd" 형식의 문자열로 반환하는 메서드입니다.
    private String getCurrentDate() {
        int year = datePciker.getYear();
        int month = datePciker.getMonth() + 1;
        int day = datePciker.getDayOfMonth();

        // 년, 월, 일을 문자열로 변환하고 "-"로 연결하여 반환합니다.
        return Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);
    }
}
