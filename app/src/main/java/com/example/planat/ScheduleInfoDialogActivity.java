package com.example.planat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ScheduleInfoDialogActivity {
    private Context context;
    String selectedDate; //선택한 날짜
    int index;//선택한 날짜의 인덱스

    public ScheduleInfoDialogActivity(Context context){
        this.context = context;
    }

    public void callFunction(String dateText, String userEmail, ArrayList<Map>date){
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

        DocumentReference curUserDate = db.collection("users").document(userEmail);
        ArrayList<Map> new_date = date; //dateContents 저장할 배열
        final Map<String,Object>dateContents = new HashMap<>(); //date field 안에 들어갈 시간 정보
        Map<String,Object>schedule = new HashMap<>(); //curUserDate에 할당할 Map 객체

        //각 위젯 정의
        final TextView schedule_text = dialog.findViewById(R.id.schedule_text);
        final ImageButton edit_button = dialog.findViewById(R.id.edit_button);
        final Button ok_button = dialog.findViewById(R.id.ok_button);


        for(int i=0;i<date.size();i++){
            String dateInfo = date.get(i).get("day").toString(); //DB의 date 필드의 i번째 index의 day 정보를 할당

            if(dateInfo.equals(dateText)){//해당 날짜의 시간을 가져와서 schedule_text에 할당
                selectedDate = dateInfo; //선택한 날짜 저장
                String startTime = date.get(i).get("startTime").toString();
                String endTime = date.get(i).get("endTime").toString();
                index = Integer.parseInt(date.get(i).get("id").toString());
                schedule_text.setText(startTime+"~"+endTime);
            }
        }

        //확인버튼 누르면 다이얼로그 끄기
        ok_button.setOnClickListener(new View.OnClickListener() {
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
                dateContents.put("day",selectedDate); //선택한 날짜 dateContents에 할당

                Dialog editDialog = new Dialog(context);

                editDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                editDialog.setContentView(R.layout.dialog_schedule);

                editDialog.show();

                Button done_button = editDialog.findViewById(R.id.done_button);
                Button cancel_button = editDialog.findViewById(R.id.cancel_button);
                EditText et_title = editDialog.findViewById(R.id.et_title);
                EditText et_time = editDialog.findViewById(R.id.et_time);

                done_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dateContents.put("id",index);
                        dateContents.put("title",et_title.getText());
                        dateContents.put("time",et_time.getText());
                        new_date.set(index,dateContents); //해당 index의 내용 갱신
                        schedule.put("date",new_date);
                        curUserDate.update(schedule);

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
