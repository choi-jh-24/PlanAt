package com.example.planat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity{
    CalendarView calendarView;
    LinearLayout listView;
    Button add_button,input_button,map_button;
    TextView text,time_text;
    Dialog dialog,dialog2; //시작시간~끝나는시간 다이얼로그
    long hour,minute,hour2,minute2;//시작시간:분 ~ 끝나는시간:분
    Button cancel_button,cancel_button2,done_button,done_button2;
    TimePicker timepicker,timepicker2;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference curUserDate = db.collection("users").document("leeseoooo@naver.com").collection("date");

    Map<String, Object> startTime = new HashMap<>();
    Map<String, Object> endTime = new HashMap<>();

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
        dialog = new Dialog(ScheduleActivity.this);//시작시간 등록다이얼로그
        dialog2 = new Dialog(ScheduleActivity.this);//끝 시간 등록다이얼로그
        dialog.setContentView(R.layout.dialog_timepicker);
        dialog2.setContentView(R.layout.dialog_timepicker);

        //다이얼로그 안에 있는 취소,완료버튼
        cancel_button = dialog.findViewById(R.id.cancel_button);
        cancel_button2 = dialog2.findViewById(R.id.cancel_button);
        done_button = dialog.findViewById(R.id.done_button);
        done_button2 = dialog2.findViewById(R.id.done_button);

        timepicker = dialog.findViewById(R.id.timepicker);
        timepicker2 = dialog2.findViewById(R.id.timepicker);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            //캘린더 날짜 변경 시
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                text.setText(year+"년"+(month+1)+"월"+day+"일");

                add_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        startActivity(new Intent(getApplicationContext(), DialogActivity.class));
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
                                startTime.put("start",year+"-"+month+"-"+day+" "+hour+":"+minute);

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
                        hour2 = timepicker2.getCurrentHour();
                        minute2 = timepicker2.getCurrentMinute();

                        timepicker2.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
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

                                startTime.put("end",year+"-"+month+"-"+day+" "+hour2+":"+minute2);
                                //curUserDate.add()
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
                startActivity(new Intent(getApplicationContext(), MiddlePlaceActivity.class));
            }
        });

    }
}

