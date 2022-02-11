package com.example.planat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    CalendarView calendarView;
    LinearLayout listView;
    Button add_button,input_button,map_button;
    TextView text,time_text;
    Dialog dialog,dialog2; //시작시간~끝나는시간 다이얼로그
    int hour,minute,hour2,minute2;//시작시간:분 ~ 끝나는시간:분

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);//달력
        listView = findViewById(R.id.listView);//최상위 LinearLayout
        text = findViewById(R.id.text); //날짜 텍스트
        time_text = findViewById(R.id.time_text); //시간 텍스트

        add_button = findViewById(R.id.add_button); //달력 - 직접입력 버튼
        input_button = findViewById(R.id.input_button); //다이얼로그 - 등록버튼
        map_button = findViewById(R.id.map_button);//지도 보는 버튼

        //커스텀 다이얼로그 생성
        dialog = new Dialog(MainActivity.this);//시작시간 등록다이얼로그
        dialog2 = new Dialog(MainActivity.this);//끝 시간 등록다이얼로그
        dialog.setContentView(R.layout.dialog_timepicker);
        dialog2.setContentView(R.layout.dialog_timepicker);

        Button cancel_button = dialog.findViewById(R.id.cancel_button);
        Button cancel_button2 = dialog2.findViewById(R.id.cancel_button);
        Button done_button = dialog.findViewById(R.id.done_button);
        Button done_button2 = dialog2.findViewById(R.id.done_button);

        TimePicker timepicker = dialog.findViewById(R.id.timepicker);
        TimePicker timePicker2 = dialog2.findViewById(R.id.timepicker);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            //캘린더 날짜 변경 시
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                text.setText(year+"년"+(month+1)+"월"+day+"일");

                add_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        Intent intent = new Intent(MainActivity.this,DialogActivity.class);
                        startActivity(intent);
                    }
                });
                input_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        dialog.show();
                        //시간 변경 이벤트 발생x 일때 초기화
                        hour = timepicker.getCurrentHour();
                        minute = timepicker.getCurrentMinute();

                        timepicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                            @Override
                            public void onTimeChanged(TimePicker timePicker, int h, int m) {
                                hour = h; minute = m; //timepicker에서 선택한 시간으로 변수 할당

                                Toast.makeText(getApplicationContext(), hour+" : "+minute, Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                });
                done_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        time_text.setText(hour+" : "+minute);
                        dialog.dismiss();//1번창 끄고
                        dialog2.show();//2번다이얼로그 켜기
                        hour2 = timePicker2.getCurrentHour();
                        minute2 = timePicker2.getCurrentMinute();

                        timePicker2.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                            @Override
                            public void onTimeChanged(TimePicker timePicker, int h, int m) {
                                hour2 =h; minute2 = m;
                            }
                        });
                        done_button2.setText("완료");
                        done_button2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                time_text.setText(time_text.getText()+" ~ "+hour2+" : "+minute2);//달력 - time_text 텍스트 갱신
                                dialog2.dismiss();
                            }
                        });
                        cancel_button2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog2.dismiss();
                            }
                        });
                    }
                });
                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }

        });

        map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MiddlePlaceActivity.class);
                startActivity(intent);
            }
        });

    }
}

