package com.example.planat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class MyPageActivity extends AppCompatActivity {
    ImageView iv;
    Uri userPhotoUri;
    String UID;
    UserModel userModel;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    Dialog dialog,dialog_withdrawal;
    Button yesBtn,noBtn,yesBtn_withdrawal,noBtn_withdrawal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        iv = findViewById(R.id.imageView2);
        getUserInfoFromServer();

        //커스텀 다이얼로그 생성
        dialog = new Dialog(MyPageActivity.this);//로그아웃 확인 다이얼로그
        dialog_withdrawal = new Dialog(MyPageActivity.this);//탈퇴 확인 다이얼로그

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_check_logout);

        dialog_withdrawal.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog_withdrawal.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_withdrawal.setContentView(R.layout.dialog_check_withdrawal);

        yesBtn = dialog.findViewById(R.id.yesBtn);
        yesBtn_withdrawal = dialog_withdrawal.findViewById(R.id.yesBtn);
        noBtn = dialog.findViewById(R.id.noBtn);
        noBtn_withdrawal = dialog_withdrawal.findViewById(R.id.noBtn);

        //프로필버튼
        findViewById(R.id.profile_button).setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();

                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(getApplicationContext(), "성공적으로 로그아웃 되었습니다!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        dialog.dismiss();
                    }
                });
            }
        });

        //탈퇴버튼
        findViewById(R.id.accountdelete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_withdrawal.show();

                yesBtn_withdrawal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
                        dialog_withdrawal.dismiss();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }
                });

                noBtn_withdrawal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog_withdrawal.dismiss();
                    }
                });
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
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}