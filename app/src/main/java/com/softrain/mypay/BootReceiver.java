package com.softrain.mypay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
// 디바이스 부팅 완료 시 자동으로 앱을 실행
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
