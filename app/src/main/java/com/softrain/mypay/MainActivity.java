package com.softrain.mypay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.softrain.mypay.utils.WIFIUtils;

import net.posprinter.IDeviceConnection;
import net.posprinter.POSConnect;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    // 앱에서 필요로 하는 권한을 정의한 문자열 배열
    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE}; // 현재는 외부 저장소에 대한 쓰기 권한
    private static final int CODE_ALL_PERMISSION = 1000; // 권한 요청 시 사용되는 요청 코드를 정의한 정수 변수
    private int mRunInXSeconds = 20;
    private boolean mOtherIsRunningFlag = false;
    private Handler mHandler = null;
    private TextView tv_counter = null;
    private TextView tv_app_version = null;
    private TextView tv_ssid = null;
    private TextView tv_ip = null;
    private Button btn_service_run = null;
    private String version_name = null;
    private int version_code_int = 0;
    private String version_code = null;
    private String and_wifi_ssid = null;
    private String and_wifi_ip = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_counter = findViewById(R.id.tv_counter);
        tv_app_version = findViewById(R.id.tv_app_version);
        tv_ssid = findViewById(R.id.tv_ssid);
        tv_ip = findViewById(R.id.tv_ip);
        btn_service_run = findViewById(R.id.btn_service_run);

        btn_service_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRunInXSeconds = 0;
            }
        });

    }  // onCreate end

    @Override
    protected void onStart() {
        super.onStart();

        mHandler = new Handler();
        mHandler.postDelayed(mStartService, 1000);

        // 앱에 외부 저장소 읽기 권한이 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_ALL_PERMISSION);
        } else {
            TextLog.o(" " + ConstDef.MYPAYT_PERMISSION_EXTERNAL_STORAGE + " Already External Storage Authorized (if): ");
            // 권한이 이미 부여되었습니다. 여기에서 파일에 액세스할 수 있습니다
            // 예를 들어 BitmapFactory.decodeFile을 사용하여 비트맵을 로드합니다
            // Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/KISMOBILE/sign_1690352925.bmp");
        }

        version_name = ConstFunc.getAppVersionName(this);
        version_code_int = ConstFunc.getAppVersionCode(this);
        version_code = Integer.toString(version_code_int);

        tv_app_version.setText("mypay 버전: " + version_name + " (" + version_code + ")");
        and_wifi_ssid = WIFIUtils.getConnectWifiSSID(this);
        if (and_wifi_ssid == null) {
            tv_ssid.setText("Android SSID: null");
        }
        else {
            tv_ssid.setText("Android SSID: " + and_wifi_ssid);
        }
        and_wifi_ip = WIFIUtils.getIpAddress(this);
        if (and_wifi_ip == null) {
            tv_ip.setText("Android IP: null");
        }
        else {
            tv_ip.setText("Android IP: " + and_wifi_ip);
        }
    }

    //     권한 요청 결과 처리, 권한이 부여되지 않은 경우 다시 권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODE_ALL_PERMISSION:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TextLog.o(" " + ConstDef.MYPAYT_PERMISSION_EXTERNAL_STORAGE + " External Storage Authorized (result): ");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(PERMISSIONS, CODE_ALL_PERMISSION);
                    }
                }
                break;
        }
    }

    private Runnable mStartService = new Runnable() {
        @Override
        public void run() {
            if (mRunInXSeconds > 0) {
                if (mOtherIsRunningFlag) {
                    tv_counter.setText(mRunInXSeconds + "초 후 창을 닫습니다");
                }
                else {
                    tv_counter.setText(mRunInXSeconds + "초 후 실행됩니다");
                }
                mRunInXSeconds--;
                mHandler.postDelayed(this, 1000);
            }
            else {
                if (mOtherIsRunningFlag) {
                    moveTaskToBack(true);
                    finish();
                }
                else {
                    TextLog.o(" " + ConstDef.MYPAYT_APP_VERSION + " version_name:" + version_name + " version_code_int:" + version_code_int + " version_code:" + version_code);
                    TextLog.o(" " + ConstDef.MYPAYT_APP_WIFE_SSID + " wifi_ssid:" + and_wifi_ssid);
                    TextLog.o(" " + ConstDef.MYPAYT_APP_WIFE_IP + " wifi_ip:" + and_wifi_ip);
                    startService(new Intent(MainActivity.this, MyPayService.class));
                    finish();
                }
            }
        }
    };







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

}
