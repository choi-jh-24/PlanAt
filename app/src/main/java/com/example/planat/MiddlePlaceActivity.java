package com.example.planat;

import android.os.Bundle;


import com.naver.maps.map.NaverMapSdk;

import androidx.appcompat.app.AppCompatActivity;

public class MiddlePlaceActivity extends AppCompatActivity {

    String CLIENT_ID = "mnu1q7kinz";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.middle_place);

        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(CLIENT_ID));

    }
}
