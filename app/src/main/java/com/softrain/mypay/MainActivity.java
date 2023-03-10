package com.softrain.mypay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int CODE_ALL_PERMISSION = 1000;

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
        }, 10000); // 60000 = 1분(60초) 후에


        if (!hasPermissions(getApplicationContext(), PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS, CODE_ALL_PERMISSION);
            }
        }
    }  // onCreate end

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
