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
            fos = new FileOutputStream(file, true);
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

































