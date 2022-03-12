package com.example.planat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyPageActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        //프로필버튼
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MypagecheckActivity.class));
            }
        });

        //로그아웃버튼
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder selectDialog = new AlertDialog.Builder(MyPageActivity.this);
                selectDialog.setTitle("로그아웃 하시겠습니까?");
                selectDialog.setMessage(" ");
                selectDialog.setPositiveButton(Html.fromHtml("예"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(getApplicationContext(), "성공적으로 로그아웃 되었습니다!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }

                });
                selectDialog.setCancelable(true);
                selectDialog.show();


            }
        });

        //탈퇴버튼
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder selectDialog = new AlertDialog.Builder(MyPageActivity.this);
                selectDialog.setTitle("탈퇴하시겠습니까?");
                selectDialog.setMessage("서버에서 정보가 모두 삭제되며 탈퇴처리 됩니다.");
                selectDialog.setPositiveButton(Html.fromHtml("<font color='#ff0000'>탈퇴하기</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "성공적으로 탈퇴되었습니다!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }

                });
                selectDialog.setCancelable(true);
                selectDialog.show();
            }
        });
    }
}