package com.softrain.mypay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    // 앱에서 필요로 하는 권한을 정의한 문자열 배열
    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE}; // 현재는 외부 저장소에 대한 쓰기 권한
    private static final int CODE_ALL_PERMISSION = 1000; // 권한 요청 시 사용되는 요청 코드를 정의한 정수 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startService(new Intent(MainActivity.this, MyPayService.class));
                finish();
            }
        }, 5000); // todo 60000 = 1분(60초) 후에

        if (!hasPermissions(getApplicationContext(), PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS, CODE_ALL_PERMISSION);
            }
        }
    }  // onCreate end

    // Context와 권한 배열을 사용하여 앱이 권한을 가지고 있는지 검사
    public boolean hasPermissions(Context _context, String[] _permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && _context != null && _permissions != null) {
            for (String permission : _permissions) {
                if (ActivityCompat.checkSelfPermission(_context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // 권한 요청 결과 처리, 권한이 부여되지 않은 경우 다시 권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODE_ALL_PERMISSION:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(PERMISSIONS, CODE_ALL_PERMISSION);
                    }
                }
                break;
        }
    }
}
