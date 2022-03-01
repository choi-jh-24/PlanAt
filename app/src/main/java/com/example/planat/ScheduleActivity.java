package com.example.planat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    MaterialCalendarView materialcalendarView;
    LinearLayout listView;
    Button add_button;
    TextView text,tv_location;
    EditText et_title,et_time;
    Dialog dialog; //일정 등록 다이얼로그
    Button cancel_button,done_button;

    FirebaseFirestore db;

    DocumentReference curUserDate;
    ArrayList<Map>date; //dateContents 저장할 배열
    Map<String,Object>dateContents = new HashMap<>(); //date field 안에 들어갈 정보
    Map<String,Object>schedule = new HashMap<>(); //curUserDate에 할당할 Map 객체

    CalendarDay m_cDay;

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
                        schedule.clear();
                        dateContents.clear();
                        Log.d("데이터", "DocumentSnapshot data: " + document.getData().get("date"));
                    } else {
                        //date field가 없는 경우 새로 생성한다.
                        date = new ArrayList<>();
                        schedule.put("date",date);
                        curUserDate.set(schedule);
                        schedule.clear();
                        Log.d("필드없음", "No such document");
                    }
                } else {
                    Log.d("실패", "get failed with ", task.getException());
                }
            }
        });


            listView = findViewById(R.id.listView);//최상위 레이아웃
            text = findViewById(R.id.text); //날짜 텍스트
            add_button = findViewById(R.id.add_button); //일정 등록버튼

            //커스텀 다이얼로그 생성
            dialog = new Dialog(ScheduleActivity.this);//시작시간 등록다이얼로그

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_schedule);

        //다이얼로그 내부 요소 초기화
        cancel_button = dialog.findViewById(R.id.cancel_button);
        done_button = dialog.findViewById(R.id.done_button);
        et_title = dialog.findViewById(R.id.et_title);
        et_time = dialog.findViewById(R.id.et_time);
        tv_location = dialog.findViewById(R.id.tv_location);

        materialcalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay cDay, boolean selected) {
                m_cDay = cDay;
                text.setText(cDay.getYear()+"-"+(cDay.getMonth()+1)+"-"+cDay.getDay());
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //edit button 눌렀는지 체크할 TextView
                        TextView flag = new TextView(ScheduleActivity.this);
                        flag.setText("false");
                        ScheduleInfoDialogActivity infoDialog = new ScheduleInfoDialogActivity(ScheduleActivity.this);
                        infoDialog.callFunction(text.getText().toString(),userEmail,date);
                    }
                });

                dateContents.put("day",cDay.getYear()+"-"+(cDay.getMonth()+1)+"-"+cDay.getDay()); //캘린더의 day 정보를 contents에 저장

                add_button.setOnClickListener(ScheduleActivity.this);
                done_button.setOnClickListener(ScheduleActivity.this);
                cancel_button.setOnClickListener(ScheduleActivity.this);
                tv_location.setOnClickListener(ScheduleActivity.this);
            }
        });
    }
    @Override
    public void onClick(View view){
        if(view == add_button){
            dialog.show();
        }
        if(view == tv_location){ //중간지점 구하기로 이동
            Intent intent = new Intent(this,MiddlePlaceActivity.class);
            startActivity(intent);
        }
        if(view == done_button){
            dialog.dismiss();//1번창 끄고
            dateContents.put("id",date.size());
            dateContents.put("title",et_title.getText());
            dateContents.put("time",et_time.getText());
            date.add(dateContents);//dateContents 추가
            schedule.put("date",date);

            curUserDate.set(schedule);
            schedule.clear();
            dateContents.clear();

            materialcalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(m_cDay)));
//            dateContents = new HashMap<>();
        }
        else if(view == cancel_button){
            dialog.dismiss();
        }
    }
}
