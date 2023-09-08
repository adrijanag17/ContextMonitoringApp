package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button symptomsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        symptomsButton = (Button) findViewById(R.id.symptoms);
        symptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSymptomsActivity();
            }
        });
    }

    public void startSymptomsActivity(){
        Intent intent = new Intent(this, SymptomsActivity.class);
        startActivity(intent);
    }
}