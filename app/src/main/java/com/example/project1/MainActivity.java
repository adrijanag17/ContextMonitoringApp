package com.example.project1;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

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

        heartRateButton.setOnClickListener(view -> {
            startVideoRecording();
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
            // Video recording completed
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


    public void startSymptomsActivity(){
        Intent intent = new Intent(this, SymptomsActivity.class);
        startActivity(intent);
    }
}

