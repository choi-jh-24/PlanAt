package com.example.planat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.Utility;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private ISessionCallback mSessionCallback;
    //define view objects
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonSignin, share_button;
    TextView textviewSingin;
    TextView textviewMessage;
    TextView textviewFindPassword;
    ProgressDialog progressDialog;
    //define firebase object
    FirebaseAuth firebaseAuth;
    DocumentReference docs;
    String userName; //유저 닉네임 SignupActivity로부터 받아오기

    //define firebase cloud store object
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSessionCallback = new ISessionCallback() {
            @Override
            public void onSessionOpened() {
                //로그인요청
                UserManagement.getInstance().me(new MeV2ResponseCallback() {
                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        //로그인 실패시
                        Toast.makeText(LoginActivity.this, "로그인 도중에 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        //세션이 닫힘
                        Toast.makeText(LoginActivity.this, "세션이 닫혔습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(MeV2Response result) {
                        //로그인 성공
                        Intent intent = new Intent(LoginActivity.this, KakoResultActivity.class);
                        intent.putExtra("name", result.getKakaoAccount().getProfile().getNickname());
                        intent.putExtra("profileImg", result.getKakaoAccount().getProfile().getProfileImageUrl());
                        intent.putExtra("email", result.getKakaoAccount().getEmail());

                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "환영 합니다!", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onSessionOpenFailed(KakaoException exception) {
                Toast.makeText(LoginActivity.this, "onSessionOpenFailed", Toast.LENGTH_SHORT).show();
            }

        };
        Session.getCurrentSession().addCallback(mSessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();

        //initializig firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

//        if (firebaseAuth.getCurrentUser() != null) {
//            Log.d("이미 로그인",firebaseAuth.getCurrentUser().toString());
//            //이미 로그인 되었다면 이 액티비티를 종료함
//            finish();
//            //그리고 profile 액티비티를 연다.
//            startActivity(new Intent(LoginActivity.this, ProfileActivity.class)); //추가해 줄 ProfileActivity
//        }

        //initializing views
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textviewSingin = (TextView) findViewById(R.id.textViewSignin);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);
        textviewFindPassword = (TextView) findViewById(R.id.textViewFindpassword);
        buttonSignin = (Button) findViewById(R.id.buttonSignup);
        progressDialog = new ProgressDialog(this);
        //final String phoneNumber = ((EditText) findViewById(R.id.Imageiv_profile)).getText().toString();
        //button click event
        buttonSignin.setOnClickListener(this);
        textviewSingin.setOnClickListener(this);
        textviewFindPassword.setOnClickListener(this);
    }
    //firebase userLogin method
    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("로그인중입니다. 잠시 기다려 주세요...");
        progressDialog.show();

        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()) {
                            finish();
                        } else {
                            textviewMessage.setText("로그인 실패 유형\n - password가 맞지 않습니다.\n -서버에러");
                        }
                    }
                });
        //유저 이메일 문서로부터 닉네임 필드 가져오기
        docs = db.collection("users").document(email);
        docs.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Object docData = document.getData().get("nickname");
                    userName = docData.toString();
                    Log.d("로그인버튼눌렀을때닉넴",userName);
                }else{
                    Log.d("출력","firebase 로딩 실패");
                }
            }
        });
        Intent intent2 = new Intent(getApplicationContext(),ScheduleActivity.class);
        intent2.putExtra("userName",userName);
        intent2.putExtra("userEmail",email);
        startActivity(intent2);
    }
    @Override
    public void onClick(View view){
        if(view == buttonSignin)
            userLogin();
        if(view == textviewSingin) {
            finish();
            startActivity(new Intent(this, SignupActivity.class));

        }
        if(view == textviewFindPassword) {
            startActivity(new Intent(this, FindActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(mSessionCallback);
    }
}
