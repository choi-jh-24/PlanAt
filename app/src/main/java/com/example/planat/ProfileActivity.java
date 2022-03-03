package com.example.planat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "ProfileActivity";

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //view objects
    private TextView tv_mymail;
    private Button btnLogout;
    private TextView textviewDelete;
    private TextView textViewRoute;
    private TextView textViewRouteToMap;
    private TextView textAppbarUser;
    private ImageView imageUser;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //activity_profile은 자체로그인(회원가입)하고 닉네임,사진 입력하는 form 거친 후에 나오는 페이지
        //after self login 은 입력 form인거죠

        //initializing views
        tv_mymail = (TextView) findViewById(R.id.tv_mail);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        textviewDelete = (TextView) findViewById(R.id.textviewDelete);
        textViewRoute = (TextView) findViewById(R.id.textViewRoute);
        textViewRouteToMap = findViewById(R.id.textViewRouteToMap);
        textAppbarUser = findViewById(R.id.tv_name);
        imageUser = findViewById(R.id.iv_photo);

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();
        //유저가 로그인 하지 않은 상태라면 null 상태이고 이 액티비티를 종료하고 로그인 액티비티를 연다.
        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        //유저가 있다면, null이 아니면 계속 진행
        user = firebaseAuth.getCurrentUser();

        /* user photo url 적용된다면 이미지 변경됩니다.  */
        Glide.with(this)
                .load(user.getPhotoUrl())
                .error(R.drawable.ico_default_image)
                .circleCrop()
                .into(imageUser);

        //textViewUserEmail의 내용을 변경해 준다.
        tv_mymail.setText("반갑습니다.\n"+ user.getEmail()+"으로 로그인 하였습니다.");

        /* user 이름을 넣어도 될것 같아요  */
//        textAppbarUser.setText(user.getDisplayName());
        textAppbarUser.setText(user.getEmail());
        //logout button event
        btnLogout.setOnClickListener(this);
        textviewDelete.setOnClickListener(this);
        textViewRoute.setOnClickListener(this);
        textViewRouteToMap.setOnClickListener(this);
        imageUser.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnLogout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        //회원탈퇴를 클릭하면 회원정보를 삭제한다. 삭제전에 컨펌창을 하나 띄워야 겠다.
        if(view == textviewDelete) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(ProfileActivity.this);
            alert_confirm.setMessage("정말 계정을 삭제 할까요?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(ProfileActivity.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                                            finish();
                                            startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                                        }
                                    });
                        }
                    }
            );
            alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(ProfileActivity.this, "취소", Toast.LENGTH_LONG).show();
                }
            });
            alert_confirm.show();
        }
        if(view == textViewRoute){ //스케줄 등록하러 가기 텍스트를 눌렀을때
            Intent intent = new Intent(getApplicationContext(), ScheduleActivity.class);
            //유저 정보 intent로 넘겨주면서 화면 이동
            intent.putExtra("userEmail",user.getEmail());
            startActivity(intent);
        }
        if(view == textViewRouteToMap){
            Intent intent = new Intent(getApplicationContext(), MiddlePlaceActivity.class);
            startActivity(intent);
        }

        if (view == imageUser) {
            Intent intent = new Intent(this, MyPageActivity.class);
            startActivity(intent);
        }
    }
}