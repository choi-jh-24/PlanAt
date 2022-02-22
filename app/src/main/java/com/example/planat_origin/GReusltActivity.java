package com.example.planat_origin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class GReusltActivity extends AppCompatActivity {

    private TextView tv_gName;
    private ImageView ic_launcher;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        String nickName = intent.getStringExtra("nickName");
        String photoURL = intent.getStringExtra("photoURL");

        tv_gName = findViewById(R.id.tv_gName);
        tv_gName.setText(nickName);

        ic_launcher = findViewById(R.id.ic_launcher);
        Glide.with(this).load(photoURL).into(ic_launcher);
    }



}
