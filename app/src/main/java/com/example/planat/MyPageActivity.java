package com.example.planat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class MyPageActivity extends AppCompatActivity {
    ImageView iv;
    Uri userPhotoUri;
    String UID;
    UserModel userModel;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        iv = findViewById(R.id.imageView2);
        getUserInfoFromServer();

        //프로필버튼
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MypagecheckActivity.class));
            }
        });

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
                        //cloud store에서 삭제
                        String myEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        db.collection("users").document(myEmail).delete();

                        //fireauth에서 삭제
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

    void getUserInfoFromServer() {
        //기존 프로필사진 불러와서 이미지뷰에 넣는 코드
        try{
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.d("FireSever", "onEvent");
                    userModel = documentSnapshot.toObject(UserModel.class);
                    if (userModel.getUserPhoto() != null && !userModel.getUserPhoto().equals("null") && !"".equals(userModel.getUserPhoto())) {
                        FirebaseStorage.getInstance().getReference("userPhoto/" + userModel.getUserPhoto()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Glide.with(MyPageActivity.this)
                                        .load(task.getResult())
                                        .circleCrop()
                                        .into(iv);
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext(), "프로필사진이 없습니다.", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}