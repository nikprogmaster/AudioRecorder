package com.example.audiorecorder.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audiorecorder.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getSupportFragmentManager().beginTransaction()
                .add(R.id.host, new RecordsFragment().newInstance())
                .commit();


    }

}


