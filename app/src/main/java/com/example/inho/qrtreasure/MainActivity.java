package com.example.inho.qrtreasure;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import com.google.zxing.Result;

import java.util.StringTokenizer;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private final static int CAMERA_PERMISSION = 1;
    private ZXingScannerView mScannerView;

    private TextView scoreVal;
    private int score;
    private int[] questions;
    private final int QUESTION_COUNT = 20;
    private final int QUESTION_OLD = 1;
    private final int QUESTION_NEW = 0;
    private final int CORRECT_ANSWER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkPermission()) {
                Toast.makeText(getApplicationContext(), "Permission already granted", Toast.LENGTH_LONG).show();
            }
            else {
                requestPermission();
            }
        }

        restoreData();

        scoreVal = findViewById(R.id.scoreVal);
        scoreVal.setText(String.format("%s 점", Integer.toString(score)));
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) ==
        PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, CAMERA_PERMISSION);
    }

    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case CAMERA_PERMISSION:
                if(grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Camera permission GRANTED", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Camera permission DENIED", Toast.LENGTH_LONG).show();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("Please allow access permission",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void QrScan(View view) {
        if(mScannerView == null)
            mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();

    }

    /*
    @Override
    public void onResume() {
        super.onResume();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkPermission()) {
                if(mScannerView == null) {
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
            }
            else {
                requestPermission();
            }
        }
    }
    */

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
        setContentView(R.layout.activity_main);
    }

    @Override
    public void handleResult(Result rawResult) {
        final String result = rawResult.getText();
        //onPause();
        // Do something with the result here
        Log.e("handler", rawResult.getText());
        Log.e("handler", rawResult.getBarcodeFormat().toString());

        checkValidCode(result);


        /*
        StringTokenizer st = new StringTokenizer(result, ",");
        int qu = Integer.parseInt(st.nextToken());
        int key = Integer.parseInt(st.nextToken());

        Toast.makeText(getApplicationContext(), String.format("Question:%d, ID:%d", qu, key), Toast.LENGTH_SHORT).show();
        mScannerView.resumeCameraPreview(MainActivity.this);

        */

        //if you would like to resume scanning, call mScannerView.resumeCameraPreview(this);
        //onResume();
        //mScannerView.resumeCameraPreview(MainActivity.this);

        onPause();
        mScannerView.stopCamera();
        setContentView(R.layout.activity_main);
        scoreVal = findViewById(R.id.scoreVal);
        scoreVal.setText(String.format("%s 점", Integer.toString(score)));
    }

    private void checkValidCode(String QRtext) {
        StringTokenizer st = new StringTokenizer(QRtext, ",");
        int qu = Integer.parseInt(st.nextToken());
        int key = Integer.parseInt(st.nextToken());

        if(questions[qu] == QUESTION_NEW) {
            questions[qu] = QUESTION_OLD;
            if (key == CORRECT_ANSWER)
                increaseScore(20);
            else
                decreaseScore(10);
        }
        else
            Toast.makeText(getApplicationContext(), "이미 시도했던 문제에요!", Toast.LENGTH_LONG).show();
    }

    private void increaseScore(int value) {
        score += value;
        Toast.makeText(getApplicationContext(), "정답이에요!", Toast.LENGTH_LONG).show();
        saveData();
    }

    private void decreaseScore(int value) {
        score -= value;
        Toast.makeText(getApplicationContext(), "땡! 다른 문제를 도전하세요!", Toast.LENGTH_LONG).show();
        saveData();
    }

    private void saveData() {
        SharedPreferences data;
        SharedPreferences.Editor editor;
        StringBuilder str = new StringBuilder();

        for(int i = 0; i < questions.length; i++)
            str.append(questions[i]).append(",");

        data = getApplicationContext().getSharedPreferences("SCORE_DATA", MODE_PRIVATE);
        editor = data.edit();
        editor.putInt("SCORE_DATA", score);
        editor.commit();

        data = getApplicationContext().getSharedPreferences("QUESTION_DATA", MODE_PRIVATE);
        editor = data.edit();
        editor.putString("QUESTION_DATA", str.toString());
        editor.commit();
    }

    private void restoreData() {
        SharedPreferences data;

        data = getApplicationContext().getSharedPreferences("SCORE_DATA", MODE_PRIVATE);
        score = data.getInt("SCORE_DATA", 0);

        questions = new int[QUESTION_COUNT];
        data = getApplicationContext().getSharedPreferences("QUESTION_DATA", MODE_PRIVATE);
        String savedString = data.getString("QUESTION_DATA", "");
        if(savedString != "") {
            StringTokenizer st = new StringTokenizer(savedString, ",");
            for (int i = 0; i < QUESTION_COUNT; i++)
                questions[i] = Integer.parseInt(st.nextToken());
        }
    }

    public void clearData(View view) {
        score = 0;
        for(int i = 0; i < questions.length; i++)
            questions[i] = 0;

        Toast.makeText(getApplicationContext(), "Data Cleared!", Toast.LENGTH_LONG).show();
        scoreVal.setText(String.format("%s 점", Integer.toString(score)));
        saveData();
    }
}
