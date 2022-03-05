package com.example.planat;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialCalendarView materialcalendarView;
    private LinearLayout listView;
    private Button add_button;
    private TextView text;
    private EditText et_title,et_time,et_location;
    private Dialog dialog; //일정 등록 다이얼로그
    private Button cancel_button,done_button;
    private ImageButton map_button;

    private FirebaseFirestore db;

    private DocumentReference docs;
    private Map<String, Object> contents = new HashMap<>(); //제목,시간,위치 집어넣을 Map
    private Map<String,Object> contentsTitle = new HashMap<>(); //클릭한 날짜를 제목으로 contents를 저장할 Map

    private CalendarDay m_cDay;

    //중간지점 구하기 눌렀을 때 result 콜백으로 받기 위한 런처
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    Intent intent = result.getData();
                    et_location.setText(intent.getStringExtra("location"));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("userEmail");
        docs = db.collection("users").document(userEmail);

        materialcalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);//달력

        //오늘 연,월,일로 m_cDay 초기화 후 해당 월의 일정 decorate
        this.handleDecorate(CalendarDay.today());

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
        map_button = dialog.findViewById(R.id.map_button);
        et_title = dialog.findViewById(R.id.et_title);
        et_time = dialog.findViewById(R.id.et_time);
        et_location = dialog.findViewById(R.id.et_location);

        materialcalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay cDay, boolean selected) {
                m_cDay = cDay;//캘린더에서 선택한 cDay로 갱신
                text.setText(cDay.getYear()+"-"+(cDay.getMonth()+1)+"-"+cDay.getDay());
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //edit button 눌렀는지 체크할 TextView
                        ScheduleInfoDialogActivity infoDialog = new ScheduleInfoDialogActivity(ScheduleActivity.this);
                        infoDialog.callFunction(m_cDay,userEmail);
                    }
                });
                add_button.setOnClickListener(ScheduleActivity.this);
                done_button.setOnClickListener(ScheduleActivity.this);
                cancel_button.setOnClickListener(ScheduleActivity.this);
                map_button.setOnClickListener(ScheduleActivity.this);
            }
        });
        //캘린더뷰에서 month를 바꿀 때마다 db에서 일정 체크표시 갱신
        materialcalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                handleDecorate(date);
            }
        });
    }
    public void handleDecorate(CalendarDay cDay){
        docs.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                contents.clear();
                contentsTitle.clear();
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.getData().isEmpty()) {// 데이터가 최소 하나 이상 저장되어 있는 경우
                        /*들어있는 데이터의 contentsTitle을 가져와서
                        연,월,일로 쪼개서 해당 날짜 CalendarDay를 구해서 decorate해준다.*/
                        Calendar calendar = Calendar.getInstance();
                        m_cDay = cDay; //초기화
                        calendar.set(m_cDay.getYear(),m_cDay.getMonth(),1);
                        //이번달의 일 수를 구한다.
                        int idx =calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                        for(int i=1;i<=idx;i++){
                            String key = m_cDay.getYear()+"-"+(m_cDay.getMonth()+1)+"-"+i;
                            //1일부터 말일까지 반복문 돌면서 DB에 해당 날짜 정보가 있는지 확인
                            if(document.getData().get(key) != null){
                                String[] dateInfoArray = key.split("-");//-기준으로 문자열 자르기
                                CalendarDay cDay = CalendarDay.from(Integer.parseInt(dateInfoArray[0]),Integer.parseInt(dateInfoArray[1])-1,Integer.parseInt(dateInfoArray[2]));
                                //DB에 저장된 모든 day 정보에 대하여 점을 찍어준다.
                                materialcalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(cDay)));
                            }
                        }
                    } else {
                        //date field가 없는 경우 새로 생성한다.
                        contentsTitle = new HashMap<>();
                        docs.set(contentsTitle);
                        //초기화
                        contents.clear();
                        contentsTitle.clear();
                    }
                }
            }
        });
    }
    @Override
    public void onClick(View view){
        if(view == add_button){
            dialog.show();
        }
        if(view == map_button){ //중간지점 구하기로 이동
            Intent intent = new Intent(this,MiddlePlaceActivity.class);
            mStartForResult.launch(intent);
        }
        if(view == done_button){
            //캘린더에서 선택한 날짜 String으로 변경
            String key = m_cDay.getYear()+"-"+(m_cDay.getMonth()+1)+"-"+m_cDay.getDay();
            contents.put("day",key);
            contents.put("title",et_title.getText().toString());
            contents.put("time",et_time.getText().toString());
            contents.put("location",et_location.getText().toString());

            contentsTitle.put(key,contents);
            docs.update(contentsTitle);

            //초기화
            contents.clear();
            contentsTitle.clear();
            et_time.setText(null);
            et_title.setText(null);

            materialcalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(m_cDay)));

            dialog.dismiss();
        }
        else if(view == cancel_button){
            dialog.dismiss();
        }
    }
}
