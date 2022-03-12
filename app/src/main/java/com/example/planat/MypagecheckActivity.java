package com.example.planat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class MypagecheckActivity extends AppCompatActivity {

    ImageView iv;
    Uri userPhotoUri;
    String UID;
    UserModel userModel;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypagecheck);

        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        TextView pe1 = findViewById(R.id.passwordEditText);
        TextView pe2 = findViewById(R.id.passwordCheckEditText);
        Button peb = findViewById(R.id.signUpButton);
        iv = findViewById(R.id.imageView);

        getUserInfoFromServer();

        peb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pe1.getText().toString().length() > 4 && pe2.getText().toString().length() >4){
                    if(pe1.getText().toString().equals(pe2.getText().toString())){

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updatePassword(pe2.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getApplicationContext(), "성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(pe2.getWindowToken(), 0);
                                onBackPressed();
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext(), "변경할 비밀번호와 비밀번호 확인란이 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }

                } else{
                    Toast.makeText(getApplicationContext(), "빈칸을 채우시거나, 6자 이상 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, 11);
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
                                Glide.with(MypagecheckActivity.this)
                                        .load(task.getResult())
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




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (requestCode == 11 && resultCode == this.RESULT_OK) {
            iv.setImageURI(data.getData());
            userPhotoUri = data.getData();
            long currentTime = System.currentTimeMillis();

            //--
            DocumentReference userRef = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            userRef
                    .update("userPhoto", UID + currentTime)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (userPhotoUri == null) {
                            } else {
                                // small image
                                try {
                                    //기존 프사 삭제
                                    //FirebaseStorage.getInstance().getReference().child("userPhoto/" + UID + currentTime).delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(userPhotoUri)
                                        .apply(new RequestOptions().override(300, 300))
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                byte[] data = baos.toByteArray();
                                                FirebaseStorage.getInstance().getReference().child("userPhoto/" + UID + currentTime).putBytes(data);

                                            }
                                        });
                            }

                            Toast.makeText(getApplicationContext(), "프로필사진이 정상적으로 변경되었습니다!", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

        }

    }



}
