package com.example.planat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialCalendarView materialcalendarView;
    private Button add_button;
    private TextView text,tv_title_info,tv_time_info,tv_location_info;
    private EditText et_title,et_time,et_location,et_title_inEtDlg,et_time_inEtDlg,et_location_inEtDlg;
    private Dialog dialog; //일정 등록 다이얼로그
    private Button cancel_button,done_button,done_button_inEtDlg,cancel_button_inEtDlg;
    private ImageButton map_button,location_button_inEtDlg,edit_button_info,close_button_info,delete_button_info;

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

    //중간지점 구하기 눌렀을 때 result 콜백으로 받기 위한 런처 => in 수정 다이얼로그
    ActivityResultLauncher<Intent> mStartForResultInEditDialog = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    Intent intent = result.getData();
                    et_location_inEtDlg.setText(intent.getStringExtra("location"));
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

        text = findViewById(R.id.text); //날짜 텍스트
        add_button = findViewById(R.id.add_button); //일정 등록버튼

        //커스텀 다이얼로그 생성
        dialog = new Dialog(ScheduleActivity.this);//시작시간 등록다이얼로그

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_schedule);

        //등록 다이얼로그 내부 요소 초기화
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
                        //정보 다이얼로그
                        final Dialog info_dialog = new Dialog(ScheduleActivity.this);
                        info_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        info_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        info_dialog.setContentView(R.layout.dialog_schedule_info);
                        info_dialog.show();

                        //각 위젯 정의
                        tv_title_info = info_dialog.findViewById(R.id.tv_title);
                        tv_time_info = info_dialog.findViewById(R.id.tv_time);
                        tv_location_info = info_dialog.findViewById(R.id.tv_location);
                        edit_button_info = info_dialog.findViewById(R.id.edit_button);
                        close_button_info = info_dialog.findViewById(R.id.close_button);
                        delete_button_info = info_dialog.findViewById(R.id.delete_button);

                        //해당 날짜 데이터 DB에서 가져오기
                        docs.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    String key = cDay.getYear()+"-"+(cDay.getMonth()+1)+"-"+cDay.getDay();
                                    String data = document.getData().get(key).toString();
                                    String[] dateInfoArray = data.split(",");

                                    String str_location = dateInfoArray[0].substring(10);
                                    String str_time = dateInfoArray[1].substring(6);
                                    String str_title = dateInfoArray[2].substring(7);

                                    //수정 시 기존 데이터로 setText 초기화해줌
                                    tv_location_info.setText(str_location);
                                    tv_time_info.setText(str_time);
                                    tv_title_info.setText(str_title);
                                }
                            }
                        });
                        //Map 초기화
                        contents.clear();
                        contentsTitle.clear();

                        //닫기 버튼 - 정보 다이얼로그 끄기
                        close_button_info.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                info_dialog.dismiss();
                            }
                        });

                        //수정 버튼(연필 이미지) 누르면 수정 다이얼로그 띄우기
                        edit_button_info.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                info_dialog.dismiss(); //정보 다이얼로그 닫고
                                Dialog editDialog = new Dialog(ScheduleActivity.this);

                                editDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                editDialog.setContentView(R.layout.dialog_schedule);

                                editDialog.show();

                                done_button_inEtDlg = editDialog.findViewById(R.id.done_button);
                                cancel_button_inEtDlg = editDialog.findViewById(R.id.cancel_button);
                                location_button_inEtDlg = editDialog.findViewById(R.id.map_button);
                                et_title_inEtDlg = editDialog.findViewById(R.id.et_title);
                                et_time_inEtDlg = editDialog.findViewById(R.id.et_time);
                                et_location_inEtDlg = editDialog.findViewById(R.id.et_location);

                                //기존 db에 저장된 정보로 초기화 해줌
                                et_title_inEtDlg.setText(tv_title_info.getText());
                                et_time_inEtDlg.setText(tv_time_info.getText());
                                et_location_inEtDlg.setText(tv_location_info.getText());

                                //수정 완료 버튼을 눌렀을 때
                                done_button_inEtDlg.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String key = m_cDay.getYear()+"-"+(m_cDay.getMonth()+1)+"-"+m_cDay.getDay();
                                        contents.put("day",key);
                                        contents.put("title",et_title_inEtDlg.getText().toString());
                                        contents.put("time",et_time_inEtDlg.getText().toString());
                                        contents.put("location",et_location_inEtDlg.getText().toString());

                                        contentsTitle.put(key,contents);
                                        docs.update(contentsTitle);

                                        //수정한 내용으로 텍스트 변경
                                        tv_title_info.setText(et_title_inEtDlg.getText());
                                        tv_time_info.setText(et_time_inEtDlg.getText());
                                        tv_location_info.setText(et_location_inEtDlg.getText());

                                        //초기화
                                        contents.clear();
                                        contentsTitle.clear();
                                        editDialog.dismiss();
                                        info_dialog.show();
                                    }
                                });
                                cancel_button_inEtDlg.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        editDialog.dismiss();
                                    }
                                });
                                //장소 등록 버튼 클릭 시 이동 => 수정 필요...
                                location_button_inEtDlg.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(ScheduleActivity.this,MiddlePlaceActivity.class);
                                        mStartForResultInEditDialog.launch(intent);
                                    }
                                });
                            }
                        });

                        //삭제 버튼 - 삭제 의사 다시 한번 묻는 다이얼로그 켜주고 예/아니오로 분기
                        delete_button_info.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Dialog delete_dialog = new Dialog(ScheduleActivity.this);
                                delete_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                delete_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                                delete_dialog.setContentView(R.layout.dialog_check_delete);
                                delete_dialog.show();

                                Button noBtn = delete_dialog.findViewById(R.id.noBtn);
                                Button yesBtn = delete_dialog.findViewById(R.id.yesBtn);

                                //삭제 취소
                                noBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        delete_dialog.dismiss();
                                    }
                                });
                                //해당 데이터 삭제
                                yesBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //지울 데이터
                                        Map<String,Object> dataFordelete = new HashMap<>();

                                        String key = cDay.getYear()+"-"+(cDay.getMonth()+1)+"-"+cDay.getDay();
                                        dataFordelete.put(key, FieldValue.delete());
                                        docs.update(dataFordelete);

                                        //열린 다이얼로그 다 닫기
                                        delete_dialog.dismiss();
                                        dialog.dismiss();

                                        //decorate 갱신을 위해 액티비티 화면 refresh
                                        Intent intent1 = getIntent();
                                        finish();
                                        startActivity(intent1);
                                    }
                                });
                            }
                        });
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
            et_location.setText(null);

            materialcalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(m_cDay)));

            dialog.dismiss();
        }
        else if(view == cancel_button){
            dialog.dismiss();
        }
    }
}
