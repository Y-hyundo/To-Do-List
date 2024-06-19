package com.cookandroid.miniproject;

// 필요한 라이브러리와 클래스들을 임포트합니다.
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// MainActivity 클래스는 앱의 첫 번째 화면을 담당합니다.
public class MainActivity extends AppCompatActivity {
    // 버튼 변수를 선언합니다.
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Activity가 생성될 때 호출됩니다.
        super.onCreate(savedInstanceState);
        // activity_main 레이아웃을 이 Activity의 사용자 인터페이스로 설정합니다.
        setContentView(R.layout.activity_main);

        // activity_main 레이아웃에서 BtnStart ID를 가진 버튼을 찾아 btnStart 변수에 할당합니다.
        btnStart = (Button) findViewById(R.id.BtnStart);
        // btnStart 버튼에 클릭 리스너를 설정합니다.
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼이 클릭되면 SelectDay Activity를 시작하는 인텐트를 생성하고 시작합니다.
                Intent intent = new Intent(getApplicationContext(), SelectDay.class);
                startActivity(intent);
            }
        });
    }
}
