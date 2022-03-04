package com.example.planat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;


public class ScheduleInfoDialogActivity {
    private Context context;

    public ScheduleInfoDialogActivity(Context context){
        this.context = context;
    }

    public void callFunction(CalendarDay cDay, String userEmail){
        //커스텀 다이얼로그 클래스 생성
        final Dialog dialog = new Dialog(context);
        //액티비티 타이틀바 숨기기+배경에 drawable 붙여줘야 border둥글게 가능
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //커스텀 다이얼로그 레이아웃 설정
        dialog.setContentView(R.layout.dialog_schedule_info);
        dialog.show();

        //firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docs = db.collection("users").document(userEmail);
        Map<String, Object> contents = new HashMap<>(); //제목,시간,위치 집어넣을 Map
        Map<String,Object> contentsTitle = new HashMap<>(); //클릭한 날짜를 제목으로 contents를 저장할 Map

        CalendarDay m_cDay = cDay;

        //각 위젯 정의
        final TextView tv_title = dialog.findViewById(R.id.tv_title);
        final TextView tv_time = dialog.findViewById(R.id.tv_time);
        final TextView tv_location = dialog.findViewById(R.id.tv_location);
        final ImageButton edit_button = dialog.findViewById(R.id.edit_button);
        final ImageButton close_button = dialog.findViewById(R.id.close_button);

        Object data; //해당 날짜에 해당하는 DB데이터

        docs.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String key = cDay.getYear()+"-"+(cDay.getMonth()+1)+"-"+cDay.getDay();
                    String data = document.getData().get(key).toString();
                    String[] dateInfoArray = data.split(",");
                    Log.d("TAG",dateInfoArray[0]+" "+dateInfoArray[1]+" "+dateInfoArray[2]);
                    // 출력결과 : {location=클릭해서 중간지점을 찾아보세요!  time=12:00-14:00  title=대통령 선거
//                    tv_title.setText();
                    }
                }
            });


        contents.clear();
        contentsTitle.clear();

        //닫기 버튼 - 다이얼로그 끄기
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //수정 버튼 누르면 다이얼로그 띄우기
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss(); //다이얼로그 닫고

                Dialog editDialog = new Dialog(context);

                editDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                editDialog.setContentView(R.layout.dialog_schedule);

                editDialog.show();

                Button done_button = editDialog.findViewById(R.id.done_button);
                Button cancel_button = editDialog.findViewById(R.id.cancel_button);
                EditText et_title = editDialog.findViewById(R.id.et_title);
                EditText et_time = editDialog.findViewById(R.id.et_time);
                TextView tv_location = editDialog.findViewById(R.id.tv_location);

                done_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String key = m_cDay.getYear()+"-"+(m_cDay.getMonth()+1)+"-"+m_cDay.getDay();
                        contents.put("day",key);
                        contents.put("title",et_title.getText().toString());
                        contents.put("time",et_time.getText().toString());
                        contents.put("location",tv_location.getText().toString());

                        contentsTitle.put(key,contents);
                        docs.update(contentsTitle);

                        //초기화
                        contents.clear();
                        contentsTitle.clear();
                        editDialog.dismiss();
                    }
                });
                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editDialog.dismiss();
                    }
                });
            }
        });
    }
}
