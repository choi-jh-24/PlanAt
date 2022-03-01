package com.example.planat;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;


import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MiddlePlaceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String CLIENT_ID = "mnu1q7kinz";
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Geocoder geocoder;
    private double latitude = 37.54647497980168;
    private double longitude = 126.96458430912304;
    private ImageButton add_button;
    private Button search_button;
    private EditText edit_text;
    private Vector<LatLng>markersPosition;
    private Button result_button;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.middle_place);

        locationSource = new FusedLocationSource(this,1000);


        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(CLIENT_ID));

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        //비동기 OnMapReadyCallback을 통해 NaverMap 객체 얻어오기
        mapFragment.getMapAsync(this);

        add_button = findViewById(R.id.add_button);
        search_button = findViewById(R.id.search_button);
        edit_text = findViewById(R.id.edit_text);
        result_button = findViewById(R.id.result_button);
        markersPosition = new Vector<LatLng>(); //초기화
    }
    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        //대중교통 레이어 그룹을 활성화
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true);

        this.naverMap = naverMap;
        geocoder = new Geocoder(this);
        //locationSource를 set하면 위치추적기능 사용가능
        naverMap.setLocationSource(locationSource);
        //위치추적 모드 지정 가능 내 위치로 이동, 현재 위치 버튼 사용가능
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        LatLng initialPosition = new LatLng(latitude,longitude);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);

        // 버튼 이벤트
        search_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Focus맞춰진 EditText에 대하여 검색버튼을 눌렀을 때 키보드 숨겨주기
                final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit_text.getWindowToken(),0);

                String str = edit_text.getText().toString();
                List<Address> addressList = null;
                try {
                    // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                    addressList = geocoder.getFromLocationName(
                            str, // 주소
                            10); // 최대 검색 결과 개수
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("출력",addressList.get(0).toString());
                // 콤마를 기준으로 split
                String []splitStr = addressList.get(0).toString().split(",");
//                String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소

                String lat = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                String lon = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도

                // 좌표(위도, 경도) 생성
                LatLng point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

                add_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 마커 생성
                        Marker marker = new Marker();
                        marker.setPosition(point);
                        // 마커 추가
                        marker.setMap(naverMap);

                        //마커 위치정보 저장
                        markersPosition.add(new LatLng(
                                Double.parseDouble(lat),Double.parseDouble(lon)
                        ));
                    }
                });

                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(point);
                naverMap.moveCamera(cameraUpdate);
            }
        });
        //중간지점 결과 버튼 클릭 시
        result_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double lat = 0,lon = 0;
                for(LatLng position : markersPosition){
                    lat += position.latitude;
                    lon += position.longitude;
                }
                LatLng point = new LatLng(lat/markersPosition.size(), lon/markersPosition.size());

                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(point);
                naverMap.moveCamera(cameraUpdate);

                Marker marker = new Marker();
                marker.setIcon(MarkerIcons.BLACK);
                //그냥 RED만 추가하면 노란색으로 보여서 색상혼합에 덧입힐 검은색으로 초기화해줌
                marker.setIconTintColor(Color.RED);
                marker.setPosition(point);
                // 마커 추가
                marker.setMap(naverMap);
            }
        });

    }
}
