package com.example.planat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class DialogActivity extends AppCompatActivity {

    PieChart pieChart;
    Button cancel_button;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_piechart);

        pieChart = findViewById(R.id.piechart);
        cancel_button = findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        pieChart.setUsePercentValues(true); //직접 퍼센트를 주는 것을 허용

        pieChart.getDescription().setEnabled(false); //desc 넣지 않음
        pieChart.setExtraOffsets(5,10,5,5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setRotationEnabled(false); //회전 애니메이션 방지

        //360도 / 24시간 => 15도씩 빈칸으로 배열값 생성, 파이차트에 삽입
        ArrayList<PieEntry> yValues = new ArrayList<>();
        int idx=0;
        while(idx++<24){
            yValues.add(new PieEntry(15f,Integer.toString(idx)));
        }
        String str = yValues.get(idx-5).getLabel();
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();

        PieDataSet dataSet = new PieDataSet(yValues," ");

        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

        dataSet.setColors(Color.rgb(26,188,156));

        PieData data = new PieData((dataSet));
        data.setValueTextSize(8f);
        data.setValueTextColor(Color.rgb(26,188,156)); //차트에 생기는 text 안보이게

        pieChart.setData(data);

        pieChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int width = pieChart.getWidth(); //974
                int height = pieChart.getHeight(); //919
                float centerX = pieChart.getCenter().x; //487
                float centerY = pieChart.getCenter().y; //459.5
                float curX = motionEvent.getX();
                float curY = motionEvent.getY();
                if(motionEvent.getX() >= centerX && motionEvent.getY() <= centerY){
                    Toast.makeText(getApplicationContext(), "1사분면", Toast.LENGTH_SHORT).show();
                }else if(motionEvent.getX() >= centerX && motionEvent.getY() >= centerY){
                    Toast.makeText(getApplicationContext(), "2사분면", Toast.LENGTH_SHORT).show();
                }else if(motionEvent.getX() <= centerX && motionEvent.getY() >= centerY){
                    Toast.makeText(getApplicationContext(), "3사분면", Toast.LENGTH_SHORT).show();
                }else if(motionEvent.getX() <= centerX && motionEvent.getY() <= centerY){
                    Toast.makeText(getApplicationContext(), "4사분면", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(), dataSet.getXValuePosition()+" , "+dataSet.getYValuePosition(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
}
