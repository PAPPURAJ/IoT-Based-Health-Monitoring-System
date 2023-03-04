package com.example.heartratemonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    TextView tempTv,bpmTv,spo2Tv,maxMinTv;
    String reports="";
    String[] report=new String[3];
    String temp="",bpm="",spo2="";
    DatabaseReference fb;
    double tempH=Double.MIN_VALUE,tempL=Double.MAX_VALUE,bpmH=Double.MIN_VALUE,bpmL=Double.MAX_VALUE,spo2H=Double.MIN_VALUE,spo2L=Double.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fb=FirebaseDatabase.getInstance().getReference("Data");
        tempTv=findViewById(R.id.temp_Tv);
        bpmTv=findViewById(R.id.bpm_tv);
        spo2Tv=findViewById(R.id.sp02_tv);
        maxMinTv=findViewById(R.id.maxMin_tv);
        loadData();
    }


    void loadData(){
        fb.child("Report").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reports=snapshot.getValue(String.class);
                report=reports.split("!");

                bpm+="\n"+report[0];
                bpmTv.setText(bpm);
                bpmH=Math.max(bpmH,convert(report[0]));
                bpmL=Math.min(bpmL,convert(report[0]));

                temp+="\n"+report[2];
                tempTv.setText(temp);
                tempH=Math.max(tempH,convert(report[2]));
                tempL=Math.min(tempL,convert(report[2]));

                spo2+="\n"+report[1];
                spo2Tv.setText(spo2);
                spo2H=Math.max(spo2H,convert(report[1]));
                spo2L=Math.min(spo2L,convert(report[1]));

                Log.e("====Data======",report[0]+" "+report[1]+" "+report[2]);

                fb.child("BPM").push().setValue(report[0]);
                fb.child("Temperature").push().setValue(report[1]);
                fb.child("SPO2").push().setValue(report[1]);

                setMaxMin();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        fb.child("BPM").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                bpm+="\n"+snapshot.getValue(String.class);
//                bpmTv.setText(bpm);
//                bpmH=Math.max(bpmH,convert(snapshot.getValue(String.class)));
//                bpmL=Math.min(bpmL,convert(snapshot.getValue(String.class)));
//                setMaxMin();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        fb.child("Temp").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                temp+="\n"+snapshot.getValue(String.class);
//                tempTv.setText(temp);
//                tempH=Math.max(tempH,convert(snapshot.getValue(String.class)));
//                tempL=Math.min(tempL,convert(snapshot.getValue(String.class)));
//                setMaxMin();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        fb.child("SpO2").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                spo2+="\n"+snapshot.getValue(String.class);
//                spo2Tv.setText(spo2);
//                spo2H=Math.max(spo2H,convert(snapshot.getValue(String.class)));
//                spo2L=Math.min(spo2L,convert(snapshot.getValue(String.class)));
//                setMaxMin();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

    }

    double convert(String v){
        try {
            return Double.parseDouble(v);
        }catch (Exception e){
            return 0.0;
        }
    }
    void setMaxMin(){
        maxMinTv.setText(tempH+"/"+tempL+"  |  "+bpmH+"/"+bpmL+"  |  "+spo2H+"/"+spo2L);
    }

}