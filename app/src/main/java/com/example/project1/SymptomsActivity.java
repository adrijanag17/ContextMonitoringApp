package com.example.project1;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class SymptomsActivity extends AppCompatActivity {

    Spinner symptomSpinner;
    RatingBar symptomRatingBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        symptomSpinner = findViewById(R.id.symptomSpinner);
        symptomRatingBar = findViewById(R.id.symptomRating);

        String[] symptomOptions = {"Nausea", "Headache", "Diarrhea",
                "Sore Throat", "Fever", "Muscle Ache", "Loss of smell or taste", "Cough",
                "Shortness of breath", "Feeling tired"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, symptomOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        symptomSpinner.setAdapter(adapter);
    }
}
