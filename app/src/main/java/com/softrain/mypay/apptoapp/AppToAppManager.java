package com.softrain.mypay.apptoapp;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 * @value appMessenger : After connection on get Messenger
 */
// 안드로이드 서비스와 통신하는데 사용
public class AppToAppManager implements AppToAppConstant {

    // 서비스 액션의 이름을 저장
    final String SERVICE_ACTION = "kr.co.kisvan.andagent.KISANDAGT";
    // 서비스 패키지의 이름을 저장
    final String SERVICE_PACKAGE = "kr.co.kisvan.andagent";

    // 클래스의 인스턴스를 저장하는 정적 변수
    private static AppToAppManager instance;
    // 객체를 저장하는 정적 변수
    private static Context mContext;

    // 서비스와 통신하기 위한 messager 객체
    private Messenger mService = null;
    // 서비스와 바인딩되었는지 여부를 나타내는 변수
    private boolean mBound;
    // 서비스와의 바인딩을 중지해야 하는지 여부를 나타내는 변수
    private boolean stop;
    // 서비스로부터 받은 데이터를 처리하기 위한 Messenger객체
    private static Messenger appMessenger;
    // 서비스로부터 받은 데이터를 애플리케이션에 전달하기 위한 핸들러 객체
    private static Handler mHandler;

    // 변수를 설정
    public static void setHandler(Handler handler) {
        mHandler = handler;
    }

    // 객체를 생성하는 메서드입니다. mContext 변수가 설정되어야 하며, instance 변수가 null인 경우에만 생성
    public static AppToAppManager with(Context context) {
        mContext = context;
        if (instance == null) {
            instance = new AppToAppManager();
        }
        if (appMessenger == null) {
            // Message생성하고 이를 핸들러에 전달한다.
            appMessenger = new Messenger(new AppToApphandler(mContext) {
                @Override
                void receivedMsg(Bundle bundle) {
                    if(mHandler != null) {
                        Message msg = Message.obtain(null, 0, 0, 0);
                        msg.setData(bundle);
                        // Message객체가 핸들러 메시지를 전송하는 메소드
                        mHandler.sendMessage(msg);
                    }
                }
            });
        }
        return instance;
    }

    // 서비스와 바인딩하기 위한 메서드 || 서비스를 찾을 수 없다면, 서비스를 계속 찾으며 바인딩을 시도
    public void startBindService() {
        stop = false; // stop 변수를 false로 초기화합니다. 이 변수는 서비스 연결 해제 시 사용
        Intent intent = new Intent();
        intent.setAction(SERVICE_ACTION); // 액션은 서비스에서 지정된 문자열 값
        intent.setPackage(SERVICE_PACKAGE); // 서비스가 위치한 애플리케이션의 패키지 이름
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE); // Context.BIND_AUTO_CREATE 플래그는 서비스가 존재하지 않을 경우 자동으로 서비스를 생성 || mConnection 객체를 통해 처리
    }

    // 서비스와 바인딩을 해제 || DISCONNETCLIENTA 메시지를 서비스로 전달
    public void stopBindService() {
        stop = true;
        // send 메서드를 이용하여 DISCONNETCLIENTA 메시지를 서비스로 전달
        if (mBound) {
            send(DISCONNETCLIENTA);
            mContext.unbindService(mConnection);
            Toast.makeText(mContext,"Service is Unbind",Toast.LENGTH_SHORT).show();
        }
    }

    // 서비스로 메시지를 전달 || what은 메시지의 타입을 나타낸다, bundle은 전달할 데이터를 저장하는 객체
    public void send(int what, Bundle bundle) {
        // 메시지에 응답할 appMessenger를 설정
        if (mBound && mService != null) {
            Message msg = Message.obtain(null, what, 0, 0);
            msg.setData(bundle);
            msg.replyTo = appMessenger;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // DISCONNETCLIENTA 메시지를 서비스로 전달
    public void send(int what) {
        if (mBound && mService != null) {
            Message msg = Message.obtain(null, what, 0, 0);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // mConnection 객체는 서비스와의 바인딩 상태를 나타내며, 서비스에 연결되었을 때와 해제되었을 때를 처리
    private ServiceConnection mConnection = new ServiceConnection() {
        // mService를 초기화하고, CHECK_ID 메시지를 서비스로 전달
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service); // IBinder 객체를 Messenger 객체로 변환하여 mService에 할당
            mBound = true;
            Bundle bundle = new Bundle();
            bundle.putString("name", "testuser01");
            bundle.putString("room", "KIS");
            // 인증을 번호와 bundle데이터를 전달함
            send(CHECK_ID, bundle);
        }

        // mService와 mBound를 초기화
        public void onServiceDisconnected(ComponentName className) {
            // stop 변수가 true이면 startBindService 메서드를 호출
            if (!stop) {
                startBindService();
            }
            mService = null;
            mBound = false;
        }
    };
}
