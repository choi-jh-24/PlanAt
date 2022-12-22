package com.example.planat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class FindActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "FindActivity";

    //define view objects
    private EditText editTextUserEmail;
    private Button buttonFind;
    private TextView textviewMessage;
    private ProgressDialog progressDialog;
    //define firebase object
    private FirebaseAuth firebaseAuth;
    private ImageView back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        editTextUserEmail = (EditText) findViewById(R.id.editTextUserEmail);
        back = findViewById(R.id.back_button);
        buttonFind = (Button) findViewById(R.id.buttonFind);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        buttonFind.setOnClickListener(this);
        back.setOnClickListener(this);

    }


        @Override
        public void onClick (View view){
            if (view == buttonFind) {
                //비밀번호 재설정 이메일 보내기
                if(editTextUserEmail.getText().toString().equals("")){
                    Toast.makeText(this, "이메일을 입력해주세요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(editTextUserEmail.getText().toString().indexOf("@")>=0){
                    progressDialog.setMessage("처리중입니다. 잠시 기다려 주세요...");
                    progressDialog.show();
                    String emailAddress = editTextUserEmail.getText().toString().trim();
                    firebaseAuth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(FindActivity.this, "이메일을 보냈습니다.", Toast.LENGTH_LONG).show();
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    } else {
                                        Toast.makeText(FindActivity.this, "메일 보내기 실패!", Toast.LENGTH_LONG).show();
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                }
                else{
                    Toast.makeText(this, "이메일 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (view == back) {
                finish();
            }
        }
    }