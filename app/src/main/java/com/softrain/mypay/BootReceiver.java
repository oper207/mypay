package com.softrain.mypay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// 디바이스 부팅 완료 시 자동으로 앱을 실행
public class BootReceiver extends BroadcastReceiver {
    //기기 부팅이 완료될 때마다 onReceive() 메서드를 호출
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED")) { // android.intent.action.BOOT_COMPLETED: 이 액션은 기기가 부팅되고 완전히 시작된 후에 발생
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Intent.FLAG_ACTIVITY_NEW_TASK 플래그를 추가하여 새로운 태스크로 액티비티를 시작합
            context.startActivity(i);
        }
    }
}
