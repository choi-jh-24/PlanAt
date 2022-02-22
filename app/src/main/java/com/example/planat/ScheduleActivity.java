package com.example.planat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity{
    MaterialCalendarView materialcalendarView;
    LinearLayout listView;
    Button input_button,map_button;
    TextView text,time_text,dialog_title,dialog_title2;
    Dialog dialog,dialog2; //시작시간~끝나는시간 다이얼로그
    long hour,minute,hour2,minute2;//시작시간:분 ~ 끝나는시간:분
    Button cancel_button,cancel_button2,done_button,done_button2;
    TimePicker timepicker,timepicker2;

    FirebaseFirestore db;

    DocumentReference curUserDate;
    ArrayList<Map>date; //dateContents 저장할 배열
    Map<String,Object>dateContents = new HashMap<>(); //date field 안에 들어갈 시간 정보
    Map<String,Object>schedule = new HashMap<>(); //curUserDate에 할당할 Map 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("userEmail");
        curUserDate = db.collection("users").document(userEmail);

        materialcalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);//달력

        //firestore에 스케줄이 저장되어있는 날짜에는 모두 decorate 해주기
        curUserDate.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.getData().get("date") != null) {//date 필드가 있는 경우(일정이 최소 하나 이상 등록된 경우)
                        date = (ArrayList<Map>) document.getData().get("date");
                        for(int i=0;i<date.size();i++){
                            String dateInfo = date.get(i).get("day").toString(); //DB의 date 필드의 i번째 index의 day 정보를 할당
                            String[] dateInfoArray = dateInfo.split("-"); //-기준으로 문자열 자르기
                            CalendarDay cDay = CalendarDay.from(Integer.parseInt(dateInfoArray[0]),Integer.parseInt(dateInfoArray[1])-1,Integer.parseInt(dateInfoArray[2]));
                            //DB에 저장된 모든 day 정보에 대하여 점을 찍어준다.
                            materialcalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(cDay)));
                        }
                        Log.d("데이터", "DocumentSnapshot data: " + document.getData().get("date"));
                    } else {
                        //date field가 없는 경우 새로 생성한다.
                        date = new ArrayList<>();
                        schedule.put("date",date);
                        curUserDate.set(schedule);
                        Log.d("필드없음", "No such document");
                    }
                } else {
                    Log.d("실패", "get failed with ", task.getException());
                }
            }
        });


        listView = findViewById(R.id.listView);//최상위 LinearLayout
        text = findViewById(R.id.text); //날짜 텍스트
        time_text = findViewById(R.id.time_text); //시간 텍스트

        input_button = findViewById(R.id.input_button); //다이얼로그 - 등록버튼
        map_button = findViewById(R.id.map_button);//지도 보는 버튼

        //커스텀 다이얼로그 생성
        dialog = new Dialog(ScheduleActivity.this);//시작시간 등록다이얼로그
        dialog2 = new Dialog(ScheduleActivity.this);//끝 시간 등록다이얼로그

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_timepicker);

        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.setContentView(R.layout.dialog_timepicker);


        //다이얼로그 타이틀 앞글자 색상 변경
        dialog_title = (TextView)dialog.findViewById(R.id.dialog_title);
        String content = dialog_title.getText().toString(); //텍스트 가져옴.
        SpannableString spannableString = new SpannableString(content); //객체 생성
        String word ="시작 시간";
        int start = content.indexOf(word);
        int end = start + word.length();
        spannableString.setSpan(new ForegroundColorSpan(Color.rgb(26,188,156)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        dialog_title.setText(spannableString);

        dialog_title2 = (TextView)dialog2.findViewById(R.id.dialog_title);
        dialog_title2.setText("⏰ 끝나는 시간을 선택해주세요");//두번째 다이얼로그 타이틀 초기화
        content = dialog_title2.getText().toString(); //텍스트 가져옴.
        spannableString = new SpannableString(content); //객체 생성
        word ="끝나는 시간";
        start = content.indexOf(word);
        end = start + word.length();
        spannableString.setSpan(new ForegroundColorSpan(Color.rgb(26,188,156)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        dialog_title2.setText(spannableString);

        //다이얼로그 안에 있는 취소,완료버튼
        cancel_button = dialog.findViewById(R.id.cancel_button);
        cancel_button2 = dialog2.findViewById(R.id.cancel_button);
        done_button = dialog.findViewById(R.id.done_button);
        done_button2 = dialog2.findViewById(R.id.done_button);
        done_button2.setText("완료");

        timepicker = dialog.findViewById(R.id.timepicker);
        timepicker2 = dialog2.findViewById(R.id.timepicker);


        materialcalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay cDay, boolean selected) {

                text.setText(cDay.getYear()+"-"+(cDay.getMonth()+1)+"-"+cDay.getDay());
                dateContents.put("day",cDay.getYear()+"-"+(cDay.getMonth()+1)+"-"+cDay.getDay()); //캘린더의 day 정보를 contents에 저장

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
                                dateContents.put("startTime",hour+":"+minute);
                            }
                        });
                    }
                });
                done_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();//1번창 끄고
                        dialog2.show();//2번다이얼로그 켜기
                        hour2 = timepicker2.getCurrentHour();
                        minute2 = timepicker2.getCurrentMinute();

                        timepicker2.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                            @Override
                            public void onTimeChanged(TimePicker timePicker, int h, int m) {
                                hour2 = h; minute2 = m;
                                dateContents.put("endTime",hour2+":"+minute2);
                            }
                        });

                        done_button2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                date.add(dateContents); //dateContents 추가
                                schedule.put("date",date);

                                curUserDate.update(schedule);

//                                time_text.setText("");//달력 - time_text 텍스트 갱신 - firestore에서 해당 day의 시간정보 끌고와서 저장
                                materialcalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(cDay)));
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
