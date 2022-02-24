package com.example.planat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class ScheduleInfoDialogActivity {
    private Context context;

    public ScheduleInfoDialogActivity(Context context){
        this.context = context;
    }

    public void callFunction(String date, String userEmail){
        //커스텀 다이얼로그 클래스 생성
        final Dialog dialog = new Dialog(context);
        //액티비티 타이틀바 숨기기
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //커스텀 다이얼로그 레이아웃 설정
        dialog.setContentView(R.layout.dialog_schedule_info);
        dialog.show();

        //각 위젯 정의
        final TextView schedule_text = dialog.findViewById(R.id.schedule_text);
        final ImageButton edit_button = dialog.findViewById(R.id.edit_button);
        final Button ok_button = dialog.findViewById(R.id.ok_button);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference curUserDate = db.collection("users").document(userEmail);

        schedule_text.setText("");

        //확인버튼 누르면 다이얼로그 끄기
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //수정 버튼 누르면...
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
