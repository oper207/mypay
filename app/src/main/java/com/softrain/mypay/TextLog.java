package com.softrain.mypay;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextLog {

    static Context mMain;
    static String mPath = "";

    // 객체를 전달받고, 외부 저장소에 로그파일을 생성하고, 시작 시간을 로그에 기록
    public static void init(Context main) {
        mMain = main;
        mPath = mMain.getExternalFilesDir(null).getAbsolutePath() + "/mypay_log.txt";
        LogResponse("---------- start time : " + getNowTime());
    }

    static String getNowTime() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formatedNow = formatter.format(now);
        return formatedNow;
    }

    // 전달받은 문자열 인자를 mPath 변수에 저장된 경로에 위치한 로그 파일에 추가
    public static void LogResponse(String text) {
        if (text == null) {
            return;
        }
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formatedNow = formatter.format(now);
        text = formatedNow + " " + text;
        File file = new File(mPath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true); // 파일에 데이터를, 파일이 없는 경우 새로 생성
            if (fos != null) {
                fos.write(text.getBytes());
                fos.write("\n".getBytes());
            }
        }
        catch (Exception e) {
        }
        finally {
            try {
                if (fos != null) fos.close();
            }
            catch (Exception e) {;}
        }
    }
}

































