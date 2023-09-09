package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SensorEventListener{


    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private long startTime, endTime;

    ArrayList<Double> accelValuesX = new ArrayList<>();
    ArrayList<Double> accelValuesY = new ArrayList<>();
    ArrayList<Double> accelValuesZ = new ArrayList<>();


    private static final int REQUEST_VIDEO_CAPTURE = 1;
    Uri videoUri;
    TextView heartRateView;
    TextView respRateView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button heartRateButton = findViewById(R.id.heartRateButton);
        heartRateView = findViewById(R.id.heartRateView);

        Button respRateButton = findViewById(R.id.respRateButton);
        respRateView = findViewById(R.id.respRateView);

        Button symptomsButton = findViewById(R.id.symptomsButton);


        heartRateButton.setOnClickListener(view -> {
            startVideoRecording();
        });

        respRateButton.setOnClickListener(view -> {
            measureRespRate();
        });

        symptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent symptomIntent = new Intent(MainActivity.this, SymptomsActivity.class);
                startActivity(symptomIntent);
            }
        });



    }

    private void startVideoRecording() {

        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        videoIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", false);
        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
        videoIntent.putExtra("android.intent.extra.flash_mode", "torch");


        startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            videoUri = data.getData();

            String videoPath = convertMediaUriToPath(videoUri);
            calcHeartRate(videoPath);

        }

    }

    public String convertMediaUriToPath(Uri uri) {
        String path = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            cursor.close();
        }

        return path;
    }

    public void calcHeartRate(String videoPath) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                String heartRate = heartHelper(videoPath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (heartRate != null){
                            heartRateView.setText(heartRate);

                        }
                    }
                });
            }
        });
    }

    public String heartHelper(String videoPath){
        Bitmap m_bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        List<Bitmap> frameList = new ArrayList<>();

        try {
            retriever.setDataSource(videoPath);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
            int intDuration = Integer.parseInt(duration);
            int i = 10;

            while (i < intDuration) {
                Bitmap bitmap = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    bitmap = retriever.getFrameAtIndex(i);
                }
                frameList.add(bitmap);
                i += 5;
            }
        } catch (Exception e) {
            Log.e("SlowTask", "Error: " + e.getMessage());
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            long redBucket = 0;
            long pixelCount = 0;
            List<Long> a = new ArrayList<>();

            for (Bitmap i : frameList) {
                redBucket = 0;

                for (int y = 550; y < 650; y++) {
                    for (int x = 550; x < 650; x++) {
                        int c = i.getPixel(x, y);
                        pixelCount++;
                        redBucket += Color.red(c) + Color.blue(c) + Color.green(c);
                    }
                }

                a.add(redBucket);
            }

            List<Long> b = new ArrayList<>();

            for (int i = 0; i < a.size() - 5; i++) {
                long temp = (a.get(i) + a.get(i + 1) + a.get(i + 2) + a.get(i + 3) + a.get(i + 4)) / 4;
                b.add(temp);
            }

            long x = b.get(0);
            int count = 0;

            for (int i = 1; i < b.size(); i++) {
                long p = b.get(i);

                if ((p - x) > 3500) {
                    count = count + 1;
                }

                x = b.get(i);
            }

            int rate = (int) ((count * 1.0f / 45) * 60);
            return String.valueOf(rate / 2);
        }

    }

    public void measureRespRate(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            Toast.makeText(this, "Sensor service not detected", Toast.LENGTH_SHORT).show();
            return;
        }
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor == null){
            Toast.makeText(this, "Accelerometer not detected", Toast.LENGTH_SHORT).show();
            return;
        }
        startTime = System.currentTimeMillis();
        endTime = startTime +  45000;        // should be 45000

        sensorManager.registerListener(this, accelerometerSensor, sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String display = "X: " + sensorEvent.values[0] + ", Y: " +
                    sensorEvent.values[1] + ", Z: " + sensorEvent.values[2];
            respRateView.setText(display);

            accelValuesX.add((double)sensorEvent.values[0]);
            accelValuesY.add((double)sensorEvent.values[1]);
            accelValuesZ.add((double)sensorEvent.values[2]);


            if (System.currentTimeMillis() > endTime) {
                sensorManager.unregisterListener(this);

                int resp = callRespiratoryCalculator();
                respRateView.setText(String.valueOf(resp));

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    private int callRespiratoryCalculator() {
        float previousValue = 0f;
        float currentValue = 0f;
        previousValue = 10f;
        int k = 0;

        int n = accelValuesZ.size();

        for (int i = 11; i < n; i++) {
            currentValue = (float) Math.sqrt(
                    Math.pow(accelValuesZ.get(i), 2.0) + Math.pow(accelValuesX.get(i), 2.0) + Math.pow(accelValuesY.get(i), 2.0)
            );

            if (Math.abs(previousValue - currentValue) > 0.15) {
                k++;
            }

            previousValue = currentValue;
        }

        double ret = k / 45.00;     // should be 45

        return (int) (ret * 30);
    }



    public void startSymptomsActivity(){
        Intent intent = new Intent(this, SymptomsActivity.class);
        startActivity(intent);
    }


}

