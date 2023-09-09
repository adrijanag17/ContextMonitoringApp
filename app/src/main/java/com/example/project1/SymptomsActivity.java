package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SymptomsActivity extends AppCompatActivity {

    Spinner symptomSpinner;
    RatingBar symptomRatingBar;
    Button uploadSymptomsButton;

    int[] symptomRatings = new int[10];
    long entryId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        Intent intent = getIntent();
        if (intent != null) {
            entryId = intent.getLongExtra("entryId", -1);
        }

        symptomSpinner = findViewById(R.id.symptomSpinner);
        symptomRatingBar = findViewById(R.id.symptomRating);
        uploadSymptomsButton = findViewById(R.id.uploadSymptomsButton);

        String[] symptomOptions = {"Nausea", "Headache", "Diarrhea",
                "Sore Throat", "Fever", "Muscle Ache", "Loss of smell or taste", "Cough",
                "Shortness of breath", "Feeling tired"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, symptomOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        symptomSpinner.setAdapter(adapter);


        symptomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, android.view.View selectedItemView, int position, long id) {
                symptomRatingBar.setRating(symptomRatings[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });


        symptomRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {
                int selectedPosition = symptomSpinner.getSelectedItemPosition();
                symptomRatings[selectedPosition] = (int)rating;
            }
        });


        uploadSymptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadSymptoms();
            }
        });
    }

    public void uploadSymptoms() {
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.addSymptoms(symptomRatings, entryId);
    }

}
