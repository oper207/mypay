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
    static final int LOG_FILE = 1;
    static final int LOG_SYSTEM = 2;
    static int mWhere = LOG_FILE | LOG_SYSTEM;
    static String mPath = "";
    static boolean mAppendTime = true;
    static int mMaxFileSize = 30 * 1000 /* KB */;
    static long mStartTime;
    static long mLastTime;

    public static void init(Context main) {
        mMain = main;
        mPath = mMain.getExternalFilesDir(null).getAbsolutePath() + "/mypay_log.txt";

        if (mMaxFileSize != 0 && (mWhere & LOG_FILE) != 0) {
            File file = new File(mPath);
            if (file.length() > mMaxFileSize * 1024) {
                String log = "";
                try {
                    FileInputStream fis = new FileInputStream(mPath);
                    int avail = fis.available();
                    byte[] data = new byte[avail];
                    while (fis.read(data) != -1) {;}
                    fis.close();
                    log = new String(data);
                }
                catch (Exception e) {;}

                log = log.substring(log.length() * 9 / 10);

                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(log.getBytes());
                    fos.close();
                }
                catch (Exception e) {;}
            }
        }

        o("---------- start time : " + getNowTime());
    }

    public static void limit() {
        if (mMaxFileSize != 0 && (mWhere & LOG_FILE) != 0) {
            File file = new File(mPath);
            if (file.length() > mMaxFileSize * 1024) {
                String log = "";
                try {
                    FileInputStream fis = new FileInputStream(mPath);
                    int avail = fis.available();
                    byte[] data = new byte[avail];
                    while (fis.read(data) != -1) {;}
                    fis.close();
                    log = new String(data);
                }
                catch (Exception e) {;}

                log = log.substring(log.length() * 9 / 10);

                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(log.getBytes());
                    fos.close();
                }
                catch (Exception e) {;}
            }
        }
    }

    public static void reset() {
        if ((mWhere & LOG_FILE) != 0) {
            File file = new File(mPath);
            file.delete();
        }

        o("---------- reset time : " + getNowTime());
    }

    static String getNowTime() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formatedNow = formatter.format(now);
        return formatedNow;
    }

    public static void o(String text, Object ... args) {
        if (mWhere == 0) {
            return;
        }

        if (text == null) {
            return;
        }

        limit();

        if (args.length != 0) {
            text = String.format(text, args);
        }

        if (mAppendTime) {
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String formatedNow = formatter.format(now);
            text = formatedNow + " " + text;
        }

        if ((mWhere & LOG_FILE) != 0 && mPath.length() != 0) {
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
                ;
            }
            finally {
                try {
                    if (fos != null) fos.close();
                }
                catch (Exception e) {;}
            }
        }

        if ((mWhere & LOG_SYSTEM) != 0) {
            Log.i(ConstDef.TAG, text);
        }
    }

    public static void lapstart(String text) {
        mStartTime = System.currentTimeMillis();
        mLastTime = mStartTime;
        o("St=0000,gap=0000 " + text);
    }

    public static void lap(String text) {
        long now = System.currentTimeMillis();
        String sText = String.format("St=%4d,gap=%4d " + text, now - mStartTime, now - mLastTime);
        mLastTime = now;
        o(sText);
    }
}
