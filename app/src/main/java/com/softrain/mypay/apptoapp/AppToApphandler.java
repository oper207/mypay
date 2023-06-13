package com.softrain.mypay.apptoapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

// 추상 클래스: : Is -A “~이다”
// Handler: 1. 안드로이드 애플리케이션에서 스레드와 메인UI 스레드 간의 통신을 담당하는 클래스이다. 2. 다른 객체들이 보낸 메시지를 받고 이를 처리하는 객체이다.
// handleMessage() 메소드를 오버라이드하며, 메시지를 처리하는 방법을 정의한 곳, 메시지의 종류에 따라 추상 메소드인 receivedMsg()를 호출하도록 정의되어 있음
public abstract class AppToApphandler extends Handler implements AppToAppConstant {

    // 앱의 자원 및 환경 정보에 액세스하고, 안드로이드 시스템 서비스와 상호 작용할 수 있음
    Context mContext;

    // 생성자 Context를 받아와서 멤버 변수 mContext에 저장
    AppToApphandler(Context context) {
        this.mContext = context;
    }

    // Message: 스레드 간에 메시지를 주고받을 때 사용되는 객체
    // Message 객체를 받아서 Bundle 객체를 추출하고, msg.what 값을 확인한 후 receivedMsg() 메소드를 호출. 메소드는 메시지의 종류를 구별한 후 그것을 처리
    @Override
    public void handleMessage(Message msg) {
        // 데이터를 저장하고 전달하는데 사용되는 객체. Key-Value 형식으로 데이터를 저장할 수 있으며, 다른 액티비티나 서비스 등으로 데이터를 전달할 때 사용
        Bundle bundle = msg.getData();
        // msg.what: 메시지를 구분하는 데 사용
        switch (msg.what) {
            case RESPONSE_DATA:
                // 메소드는 추상 메소드이며, 구현되지 않았기 때문에 하위 클래스에서 반드시 구현
                receivedMsg(bundle);
                break;
            default:
                super.handleMessage(msg);
        }
    }

    abstract void receivedMsg(Bundle bundle);
}