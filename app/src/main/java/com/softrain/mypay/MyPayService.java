package com.softrain.mypay;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class MyPayService extends Service {

    // BroadcastReceiver 객체로, mydisplay에서 데이터를 수신할 때 사용
    private final BroadcastReceiver bMdCommand;
    // 유형(type)
    private String mCurrType = null;
    // 함수명(function)
    private String mCurrFuncNm = null;
    // (name)
    private String mCurrData = null;
    // 문자열 변수로, 앱의 이름을 저장
    String appName = "kr.co.kisvan.andagent.inapp";
    // KisvanSpec 객체로, Kisvan 관련 스펙을 저장하고 사용
    KisvanSpec kisvanSpec;
    // 문자열 변수로, 인덱스를 저장
    String divIndex = null;
    // 지불(payment)에 대한 응답(response)을 저장
    AgentResPayment rPayment;
    // 쉬운 지불(easy payment)에 대한 응답을 저장
    AgentResEasyPayment rEasyPayment;
    // 시리얼 포트의 이름을 저장
    static final String SERIAL_PORT_NAME= "ttyS4";
    // 시리얼 통신의 전송 속도(baud rate)를 저장
    static final int SERIAL_BAUDRATE= 9600;
    // 시리얼 포트와의 통신을 관리 객체
    SerialPort serialPort;
    // 데이터를 주고받기 위한 객체
    InputStream inputStream;
    OutputStream outputStream;
    // 시리얼 통신을 처리하는 스레드를 관리
    SerialThread serialThread;
    // 다른 컴포넌트로 데이터를 전달하기 위해 사용
    Intent intent;
    // 간편결제 구분 변수
    boolean codeAndQR = false;
    // 바코드 메소드 한번만 실행되게 하는 변수
    boolean bRunOnce = false;
    // 바코드 값
    private String qrAndBardCodeData = "";
    // 큐알, 바코드 3초뒤에 한번만 실행
    private boolean runAfter3Seconds = false;
    // 큐알 사이즈 체크 변수
    boolean qrSizeCheck = false;
    // 간편결제 데이터 더할때 기달려주는 핸들러
    private Handler easyPaymentHandler = new Handler();
    // 연속결제 방지하는 변수
    boolean isStarting;

    public MyPayService() {

        // mydisplay에서 결제 받아오는 곳
        bMdCommand = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // mydisplay에서 결제 데이터값 받는 곳
                payHandleRequest(intent);
            }
        };
    }

    @Override
    public synchronized void onCreate() {
        super.onCreate();
        TextLog.init(this); // 로그 초기화
        PrintFactory.init(this); // PrintFactory 클래스를 초기화
        kisvanSpec = new KisvanSpec(); // KisvanSpec 클래스의 인스턴스를 생성하여 kisvanSpec 변수에 할당
        SetSerialPort(SERIAL_PORT_NAME); // 사용하여 시리얼 포트를 설정
        StartRxThread(); // 수신 쓰레드를 시작
        isStarting = false;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                intent = new Intent(appName);
                kisvanSpec.Init();
                kisvanSpec.inTestMode = "Y";
                kisvanSpec.inTranCode = "LT";
                kisvanSpec.RequestData(intent);
                intent.putExtra("ResultType", 1);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(appName);
                rPayment = new AgentResPayment();
                registerReceiver(rPayment, intentFilter);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }, 2000);
    }

    // mydisplay에서 결제 데이터값 받는 곳
    private void payHandleRequest(Intent intent){
        mCurrType = intent.getStringExtra("type");
        mCurrFuncNm = intent.getStringExtra("funcNm");
        mCurrData = intent.getStringExtra("data");
        TextLog.LogResponse(" " + ConstDef.MYDISPLAY_TO_MYPAY_TAG + " " + mCurrType + " " + mCurrFuncNm + " " + mCurrData);
        // 결제
        if(mCurrFuncNm.equals("$creditApproval")) {
            if(isStarting) {
                return;
            }
            isStarting = true;
            creditApproval(mCurrData);
        }
        // 프린터
        else if(mCurrFuncNm.equals("$printReceipt")){
            printReceipt(mCurrData);
        }
        // 결제취소
        else if(mCurrFuncNm.equals("$cancelApproval")){
            cancelApproval(mCurrData);
        }
    }

    // 결제 값 추출
    private void creditApproval(String mCurrData) {
        try {
            JSONObject jsonObject = new JSONObject(mCurrData);
            String money = jsonObject.getString("money");
            String tax = jsonObject.getString("tax");
            String tCode = jsonObject.getString("tcode");
            divIndex = jsonObject.getString("divIndex");
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(mCurrType) " + mCurrType);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(mCurrFuncNm) " + mCurrFuncNm);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(mCurrData) " + mCurrData);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(rcode) " + tCode);
//            codeAndQR = true;
            // 카드결제
            if(tCode.equals("D1")) {
                TextLog.LogResponse(" " + ConstDef.MYDISPLAY_TO_MYPAY_GENERAL_PAYMENT_TAG + " " + mCurrType + " " + mCurrFuncNm + " " + mCurrData);
                // 결제창 띄우고 결제 처리 메소드
                requestData(money, tax, tCode);
            }
            // 간편결제
            else if(tCode.equals("AC")) {
                codeAndQR = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 결제취소
    private void cancelApproval(String mCurrData) {
        try {
            JSONObject jsonObject = new JSONObject(mCurrData);
            String money = jsonObject.getString("money");
            String tax = jsonObject.getString("tax");
            String tCode = jsonObject.getString("tcode");
            String aNumber = jsonObject.getString("approvalNumber");
            String aDate = jsonObject.getString("approvalDate");
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(mCurrType) " + mCurrType);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(mCurrFuncNm) " + mCurrFuncNm);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(mCurrData) " + mCurrData);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(rcode) " + tCode);
            TextLog.LogResponse(" " + ConstDef.MYDISPLAY_TO_MYPAY_CANCEL_PAYMENT_TAG + " " + mCurrType + " " + mCurrFuncNm + " " + mCurrData);

            // 결제창 취소창 띄우고 결제취소 처리 메소드
            payCancel(money, tax, tCode, aNumber, aDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 프린터 값 추출
    private void printReceipt(String mCurrData) {
        // 영수증 데이터 처리
        try {
            JSONObject jsonObject = new JSONObject(mCurrData);
            String rReceiptPageNum = jsonObject.getString("receiptPageNum");
            String rStoreInfo = jsonObject.getString("storeInfo");
            String rReceiptInfo = jsonObject.getString("receiptInfo");
            String rCardInfo = jsonObject.getString("cardInfo");
            String rBill = jsonObject.getString("billInfo");
            Log.e("MyService: mydisplay로부터", " 받아온 영수증 값 : "+rReceiptPageNum+rStoreInfo+rReceiptInfo+rCardInfo+rBill + " json : " + jsonObject);
            TextLog.LogResponse(" " + ConstDef.MYDISPLAY_TO_MYPAY_PRINT_TAG + " " + mCurrType + " " + mCurrFuncNm + " " + mCurrData);
            PrintFactory.funcPrintText(rReceiptPageNum, rStoreInfo, rReceiptInfo, rCardInfo, rBill);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // 알림을 생성하여 표시하는 메서드
        showNotification();
        // 결제 Intent필터 객체생성
        IntentFilter filterMdCommand = new IntentFilter();
        filterMdCommand.addAction(ConstDef.ACTION_NAME_FROM_MYDISPLAY);
        registerReceiver(bMdCommand, filterMdCommand);

        return START_STICKY; // START_STICKY 값을 반환하여 시스템이 서비스를 종료한 후에도 자동으로 서비스를 재시작하도록 지정
    }

    @Override
    public synchronized void onDestroy() {
        // 연결 끊기
        unregisterReceiver(bMdCommand);
        PrintFactory.printerDestroy();
        serialPort.close();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //  앱이 백그라운드에서 실행되는 동안 알림을 유지하고자 할 때 사용
    @SuppressLint("NewApi")  // .setColor(Color.BLACK)에 관한 경고를 무시하도록 지정. NewApi는 여기서 최신 버전의 안드로이드 API를 가리킴
    private void showNotification() {

        int notification_id = 1;
        String channel_id = "default";

        // Oreo(버전 8.0) 이상과 그 이하의 버전에 따라 다른 방식으로 알림을 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_id, "기본 채널", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            stopForeground(true);

            Notification notification = new NotificationCompat.Builder(this, channel_id)
                    .setContentTitle("SOFTRAIN")
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setAutoCancel(false)
                    .setWhen(System.currentTimeMillis())
                    .setColor(Color.BLACK)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build();

            notification.flags |= Notification.FLAG_NO_CLEAR;

            startForeground(notification_id, notification);
        }
        else {
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.cancelAll();

            Notification notification = null;
            notification = new Notification.Builder(this)
                    .setContentTitle("SOFTRAIN")
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setAutoCancel(false)
                    .setWhen(System.currentTimeMillis())
                    .setColor(Color.BLACK)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .build();

            notification.flags |= Notification.FLAG_NO_CLEAR;

            notificationManager.notify(notification_id, notification);
        }
    }

    // 결제창 띄우고 결제 처리
    @SuppressLint("SuspiciousIndentation")
    public void requestData(String money, String tax, String tCode) {
        intent = new Intent(appName); // 앱 이름을 가지고 Intent 객체를 생성합니다.
        kisvanSpec.Init(); // KisvanSpec 객체의 Init() 메서드를 호출하여 초기화 작업을 수행합니다.
        intent.putExtra("inDefaultCatId", true); // Intent에 "inDefaultCatId"라는 키로 true 값을 추가합니다.
        kisvanSpec.inTestMode = "Y"; // 테스트 모드를 활성화하기 위해 "Y" 값을 할당합니다.
        // 빈 값으로 입력하면 AndroidAgent 내에서 선택된 가맹점으로 결제가 진행됩니다.
        kisvanSpec.inTranCode = tCode; // 거래 코드를 설정합니다.
        kisvanSpec.inTotAmt = String.valueOf(Integer.parseInt(money) + Integer.parseInt(tax));
        kisvanSpec.inVatAmt = tax; // 부가세 금액을 설정합니다.
        kisvanSpec.RequestData(intent); // KisvanSpec 객체의 RequestData() 메서드를 호출하여 intent와 관련된 데이터를 설정합니다.
        intent.putExtra("ResultType", 1); // Intent에 "ResultType"이라는 키로 1 값을 추가합니다. (0 : Activity, 1 : BroadCastReceiver)
        IntentFilter intentFilter = new IntentFilter(); // IntentFilter 객체를 생성합니다.
        intentFilter.addAction(appName); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
        rPayment = new AgentResPayment(); // AndroidAgentRes 클래스의 인스턴스를 생성하여 receiver 변수에 할당합니다.
        registerReceiver(rPayment, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent); // Intent를 사용하여 액티비티를 시작합니다.
        TextLog.LogResponse(" " + ConstDef.KIS_PAYMENT_REQUEST_TAG + " inTestMode: " + kisvanSpec.inTestMode + " inTranCode: " + kisvanSpec.inTranCode + " inTotAmt: " + kisvanSpec.inTotAmt + " inVatAmt: " + kisvanSpec.inVatAmt);
    }

    // 방송 인텐트를 수신하는 역할
    public class AgentResPayment extends BroadcastReceiver {

        // 방송을 수신하면 호출되는 콜백 메서드
        @Override
        public void onReceive(Context context, Intent intent) {
            // 신자를 등록 해제
            unregisterReceiver(rPayment);
            // 인텐트에 대한 응답을 처리
            PaymentResponse(intent);
        }
    }

    // 카드 결제
    public void PaymentResponse(Intent intent) {
        if(intent == null) return; // Intent가 null인 경우 종료
        kisvanSpec.ResponseData(intent); // kisvanSpec 객체로부터 데이터 가져오기
        StringBuilder sb = new StringBuilder();
            // 결제 실행시 응답값 받는 곳
            sb.append(divIndex)
                    .append("#")
                    .append(kisvanSpec.outReplyCode) // 응답코드
                    .append("#")
                    .append(kisvanSpec.outReplyMsg1) // 응답메시지1
                    .append("#")
                    .append(kisvanSpec.outReplyMsg2) // 응답메시지2(언제 값이 나올까?)
                    .append("#")
                    .append(kisvanSpec.outTotAmt)  // 승인 금액
                    .append("#")
                    .append(kisvanSpec.outVatAmt) // 승인 부가세
                    .append("#")
                    .append(kisvanSpec.outAuthNo) // 승인번호
                    .append("#")
                    .append(kisvanSpec.outAuthDate) // 승인일자(YYYYMMDD)
                    .append("#")
                    .append(kisvanSpec.outIssuerName) // 발급사 명
                    .append("#")
                    .append(kisvanSpec.outCardNo) // 카드빈번호(6자리)
                    .append("#")
                    .append(kisvanSpec.outMerchantRegNo) // 카드사 가맹점 번호
                    .append("#")
                    .append(kisvanSpec.outIssuerCode) // 발급사 코드
                    .append("#")
                    .append(kisvanSpec.outWCC) // 카드타입
                    .append("#")
                    .append(kisvanSpec.outCardBrand) // 카드브랜드
                    .append("#")
                    .append(kisvanSpec.outSignYn) // 서명여부
                    .append("#")
                    .append(kisvanSpec.outSvcAmt) // 승인 봉사료
                    .append("#")
                    .append(kisvanSpec.outJanAmt) // 잔액
                    .append("#")
                    .append(kisvanSpec.outAccepterCode) // 매입사 코드
                    .append("#")
                    .append(kisvanSpec.outAccepterName) // 매입사 명
                    .append("#")
                    .append(kisvanSpec.outVanKey) // 거래 고유 번호(취소 시 사용가 능)
                    .append("#")
                    .append(kisvanSpec.outPayType) // 결제 유형
                    .append("#")
                    .append(kisvanSpec.outPayGubun)
                    .append("#")
                    .append(kisvanSpec.outUserID)
                    .append("#")
                    .append(kisvanSpec.outOrderNo)
                    .append("#")
                    .append(kisvanSpec.outMemberShipBarcodeNumber)
                    .append("#")
                    .append(kisvanSpec.outOTC) // OTC
                    .append("#")
                    .append(kisvanSpec.outPosSerialNo) // POS 일련번호
                    .append("#")
                    .append(kisvanSpec.outCatId) // Cat ID
                    .append("#")
                    .append(kisvanSpec.outInstallMent)
                    .append("#")
                    .append(kisvanSpec.outTradeNumber) // 거래 번호
                    .append("#")
                    .append(kisvanSpec.outAccountNumber)
                    .append("#")
                    .append(kisvanSpec.outBarcodeNumber)
                    .append("#")
                    .append(kisvanSpec.outDiscountPoint)
                    .append("#")
                    .append(kisvanSpec.outUsedPoint)
                    .append("#")
                    .append(kisvanSpec.outStatusICCard);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("divIndex: " + divIndex);
        System.out.println("outReplyCode: " + kisvanSpec.outReplyCode);
        System.out.println("outReplyMsg1: " + kisvanSpec.outReplyMsg1);
        System.out.println("outReplyMsg2: " + kisvanSpec.outReplyMsg2);
        System.out.println("outTotAmt: " + kisvanSpec.outTotAmt);
        System.out.println("outVatAmt: " + kisvanSpec.outVatAmt);
        System.out.println("outAuthNo: " + kisvanSpec.outAuthNo);
        System.out.println("outAuthDate: " + kisvanSpec.outAuthDate);
        System.out.println("outIssuerName: " + kisvanSpec.outIssuerName);
        System.out.println("outCardNo: " + kisvanSpec.outCardNo);
        System.out.println("outMerchantRegNo: " + kisvanSpec.outMerchantRegNo);
        System.out.println("outIssuerCode: " + kisvanSpec.outIssuerCode);
        System.out.println("outWCC: " + kisvanSpec.outWCC);
        System.out.println("outCardBrand: " + kisvanSpec.outCardBrand);
        System.out.println("outSignYn: " + kisvanSpec.outSignYn);
        System.out.println("outSvcAmt: " + kisvanSpec.outSvcAmt);
        System.out.println("outJanAmt: " + kisvanSpec.outJanAmt);
        System.out.println("outAccepterCode: " + kisvanSpec.outAccepterCode);
        System.out.println("outAccepterName: " + kisvanSpec.outAccepterName);
        System.out.println("outVanKey: " + kisvanSpec.outVanKey);
        System.out.println("outPayType: " + kisvanSpec.outPayType);
        System.out.println("outPayGubun: " + kisvanSpec.outPayGubun);
        System.out.println("outUserID: " + kisvanSpec.outUserID);
        System.out.println("outOrderNo: " + kisvanSpec.outOrderNo);
        System.out.println("outMemberShipBarcodeNumber: " + kisvanSpec.outMemberShipBarcodeNumber);
        System.out.println("outOTC: " + kisvanSpec.outOTC);
        System.out.println("outPosSerialNo: " + kisvanSpec.outPosSerialNo);
        System.out.println("outCatId: " + kisvanSpec.outCatId);
        System.out.println("outInstallMent: " + kisvanSpec.outInstallMent);
        System.out.println("outTradeNumber: " + kisvanSpec.outTradeNumber);
        System.out.println("outAccountNumber: " + kisvanSpec.outAccountNumber);
        System.out.println("outBarcodeNumber: " + kisvanSpec.outBarcodeNumber);
        System.out.println("outDiscountPoint: " + kisvanSpec.outDiscountPoint);
        System.out.println("outUsedPoint: " + kisvanSpec.outUsedPoint);
        System.out.println("outStatusICCard: " + kisvanSpec.outStatusICCard);

        TextLog.LogResponse(" " + ConstDef.KIS_PAYMENT_RESPONSE_TAG + " " + sb);
        String sbPaydatas = sb.toString();
        String[] split = sbPaydatas.split("#"); // #를 제외한 단어들을 배열로 추출
        JSONObject jsonObject = new JSONObject();
        int number = 0;
        for (int i = 0; i < split.length; i++) {
            String value = split[i];
            if (!value.equals("null") && !value.isEmpty()) {
                String key = "response" + number;
                try {
                    jsonObject.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                number++;  // k 변수를 1씩 증가시킴
            }
        }
        handleSendMain(jsonObject);
    }

    private void handleSendMain(JSONObject jsonObject) {
        Log.i(ConstDef.TAG," MyService 리더기 응답 값"+  jsonObject.toString());
        final Intent intent = new Intent(ConstDef.ACTION_NAME_TO_MYDISPLAY);
        intent.putExtra("type", ConstDef.CMD_TYPE);
        intent.putExtra("funcNm", ConstDef.CMD_FUNCNM_INTENT_RECEIVED);
        intent.putExtra("data", jsonObject.toString());
        sendBroadcast(intent);
        TextLog.LogResponse(" " + ConstDef.MYPAYT_TO_MYDISPLAY_TAG + " " + ConstDef.ACTION_NAME_TO_MYDISPLAY + " " +ConstDef.CMD_TYPE + " " + ConstDef.CMD_FUNCNM_INTENT_RECEIVED + " " + jsonObject);
        UndoContinuous();
    }

    // todo 간편결제
    void SetSerialPort(String name) {
        // 이용하여 현재 연결된 모든 시리얼 포트의 이름과 경로를 가져옴
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        String[] devices = serialPortFinder.getAllDevices();
        String[] devicesPath = serialPortFinder.getAllDevicesPath();

        for(int i = 0; i < devices.length; i++) {
            String device = devices[i];
            // 가져온 시리얼 포트 이름들 중에서 인자로 전달된 name을 포함한 포트를 찾는다/
            if(device.contains(name)) {
                try {
                    // SerialPort 클래스의 객체로 생성
                    serialPort = new SerialPort(new File(devicesPath[i]),SERIAL_BAUDRATE,0);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // serialPort 멤버 변수가 null이 아니면
        if(serialPort != null) {
            // inputStream과 outputStream 멤버 변수에 각각 할당
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
        }
    }
    // 입력 스트림이 null 이면 로그를 출력하고 함수를 종료한다.
    void StartRxThread() {
        if(inputStream == null) {
            Log.e("SerialExam", "Can`t open inputstream");
            return;
        }
// SerialThread 객체를 생성하고 시작한다.
        serialThread = new SerialThread();
        serialThread.start();
    }

    public class AgentResEasyPayment extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(rEasyPayment);
            PosResponse2(intent);
        }
    }

    class SerialThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    byte[] buffer = new byte[128];
                    int size = inputStream.read(buffer);
                    if(size > 0 && codeAndQR)  {
                        String receivedData = null;
                        receivedData = new String(buffer, 0, size);
                        Log.e("간편 오리지널 값 0", " : " +receivedData + " size: " + size);
                        int dataSize = 0;
                        byte bufferData[] = new byte[128];

                        byte[] rbufferData = receivedData.getBytes("UTF-8"); // UTF-8   CP949
                        for (int j = 0; j < rbufferData.length; j++) {
                            if (rbufferData[j] >= 0x20 && rbufferData[j] <= 0x7e) {
                                bufferData[dataSize++] = rbufferData[j];
                            }
                        }
                        String converted = new String(bufferData, 0, dataSize);
                        Log.e("간편 변환된 값 1", " : " +converted + " size: " + size);
                        OnReceiveData(converted, size);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void OnReceiveData(String converted, int size) {
            // 숫자가 20개 이상 포함되어 있는지 확인
            boolean hasDigitsOver20 = isDigitsOver20(converted); // todo 제로페이 취소 시 바코드번호가 12자리라서 바코드, QR구분 메소드변경해야된다.
            if (hasDigitsOver20) {
                if(!bRunOnce) {
                    BarCodeCheck(converted);
                }
                System.out.println("문자열에 20개 이상의 숫자가 포함되어 있습니다."+size);
            } else {
                if(!qrSizeCheck) {
                    QrCheck(converted, size);
                }
                System.out.println("문자열에 20개 이상의 숫자가 포함되어 있지 않습니다."+size);
            }
    }

    // 입력된 문자열에 숫자가 20개 이상 포함되어 있는지를 확인하는 메소드
    public static boolean isDigitsOver20(String input) {
        int count = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i); //  문자열 input에서 인덱스 i에 해당하는 위치의 문자를 가져와서 c 변수에 할당하는 구문
            // c가 숫자인지 여부를 판단
            if (Character.isDigit(c)) {
                count++;
                if (count >= 20) {
                    return true;
                }
            }
        }
        return false;
    }

    void BarCodeCheck(String converted) {
        bRunOnce = true;
        String specialCharacterRemovalValue = removeSpecialCharacters(converted); // 특수 문자를 제거하는 메소드
        String englishCharacterRemovalValue = removeEnglishCharacters(specialCharacterRemovalValue);   // 영문자를 제거하는 메소드

        qrAndBardCodeData += englishCharacterRemovalValue;
        if (!runAfter3Seconds) {
            runAfter3Seconds = true;
            easyPaymentHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EasyPaymentResponse(qrAndBardCodeData);
                    runAfter3Seconds = false; // runAfter3Seconds 실행이 완료되면 플래그 변수 변경
                }
            }, 2000);
        }
    }

    // 특수 문자를 제거하는 메소드
    public String removeSpecialCharacters(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i); // input 문자열에서 인덱스 i에 해당하는 문자를 가져
            // ch가 영문자(알파벳) 또는 숫자인지 여부를 판단
            if (Character.isLetterOrDigit(ch)) // ch가 영문자나 숫자라면, 조건식은 true를 반환 || ch가 특수 문자나 공백 등의 문자라면, 조건식은 false를 반환
            {
                result.append(ch); // 문자열에 문자 ch를 추가하는 역할
            }
        }
        return result.toString();
    }

    // 영문자를 제거하는 메소드
    public String removeEnglishCharacters(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i); //  // input 문자열에서 인덱스 i에 해당하는 문자를 가져
            // ch가 영문자인지 확인
            if (Character.isLetter(ch)) {
                continue;
            }
            result.append(ch); // 문자열에 문자 ch를 추가하는 역할
        }
        return result.toString();
    }

    void QrCheck(String buffer, int size) {
        Log.e("간편 큐알 값 1", " : " +buffer);
        if(size != 32) {
            qrSizeCheck = true;
        }
        String QuotesRemovalVaule = removeQuotes(buffer); // "제거하는 메소드
        qrAndBardCodeData += QuotesRemovalVaule;
        Log.e("간편 큐알 더한 값 2", " : " + qrAndBardCodeData);
        // qrtest 실행 중이 아닌 경우에만 3초 후에 qrtest 메소드 실행
        if (!runAfter3Seconds) {
            runAfter3Seconds = true;
            easyPaymentHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EasyPaymentResponse(qrAndBardCodeData);
                    runAfter3Seconds = false; // qrtest 실행이 완료되면 플래그 변수 변경
                }
            }, 2000);
        }
    }

    // "제거하는 메소드
    public String removeQuotes(String input) {
        return input.replaceAll("\"", "");
    }

    void EasyPaymentResponse(String setdata) {
        Log.e("간편 마지막 값", " : " + setdata);
//        intent = new Intent(appName);
//        kisvanSpec.Init();
//        kisvanSpec.inTestMode = "N";
//        intent.putExtra("inFullScreenFlag", false);
//        kisvanSpec.inTotAmt = "1004";
//        kisvanSpec.inVatAmt = "91";
//        kisvanSpec.inTranCode = "RR";
//        kisvanSpec.inWCC = "K";
//        kisvanSpec.inOrgAuthDate = "20230606";
//        kisvanSpec.inOrgAuthNo = "43033181";
//        kisvanSpec.inBarcodeNumber = String.format("%03d", addData.length()) + addData;
//        kisvanSpec.RequestData(intent);
//        intent.putExtra("ResultType", 1);
//        IntentFilter intentFilter = new IntentFilter(); // IntentFilter 객체를 생성합니다.
//        intentFilter.addAction(appName); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
//        rEasyPayment = new AgentResEasyPayment(); // AndroidAgentRes 클래스의 인스턴스를 생성하여 receiver 변수에 할당합니다.
//        registerReceiver(rEasyPayment, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
//        startActivity(intent);
          // todo test
        Intent intent = new Intent(appName);
        kisvanSpec.Init();
        kisvanSpec.inTestMode = "Y";
        intent.putExtra("inFullScreenFlag", false);
        //총금액
        kisvanSpec.inTotAmt = "1004";
        //부가세
        kisvanSpec.inVatAmt = "91";
        kisvanSpec.inTranCode = "AC";
        kisvanSpec.inWCC = "K";
        kisvanSpec.inBarcodeNumber = String.format("%03d", setdata.length()) + setdata;
        kisvanSpec.RequestData(intent);
        intent.putExtra("ResultType", 1);
        IntentFilter intentFilter = new IntentFilter(); // IntentFilter 객체를 생성합니다.
        intentFilter.addAction(appName); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
        rEasyPayment = new AgentResEasyPayment(); // AndroidAgentRes 클래스의 인스턴스를 생성하여 receiver 변수에 할당합니다.
        registerReceiver(rEasyPayment, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
        startActivity(intent);
    }

    public void PosResponse2(Intent intent) {
        if(intent == null) return; // Intent가 null인 경우 종료
        kisvanSpec.ResponseData(intent); // kisvanSpec 객체로부터 데이터 가져오기
        StringBuilder sb = new StringBuilder();
        // 결제 실행시 응답값 받는 곳
        sb.append(divIndex)
                .append("#")
                .append(kisvanSpec.outReplyCode) // 응답코드
                .append("#")
                .append(kisvanSpec.outReplyMsg1) // 응답메시지1
                .append("#")
                .append(kisvanSpec.outReplyMsg2) // 응답메시지2
                .append("#")
                .append(kisvanSpec.outTotAmt)  // 승인 금액
                .append("#")
                .append(kisvanSpec.outVatAmt) // 승인 부가세
                .append("#")
                .append(kisvanSpec.outAuthNo) // 승인번호
                .append("#")
                .append(kisvanSpec.outAuthDate) // 승인일자(YYYYMMDD)
                .append("#")
                .append(kisvanSpec.outIssuerName) // 발급사 명
                .append("#")
                .append(kisvanSpec.outCardNo) // 카드빈번호(6자리)
                .append("#")
                .append(kisvanSpec.outMerchantRegNo) // 카드사 가맹점 번호
                .append("#")
                .append(kisvanSpec.outIssuerCode) // 발급사 코드
                .append("#")
                .append(kisvanSpec.outWCC) // 카드타입
                .append("#")
                .append(kisvanSpec.outCardBrand) // 카드브랜드
                .append("#")
                .append(kisvanSpec.outSignYn) // 서명여부
                .append("#")
                .append(kisvanSpec.outSvcAmt) // 승인 봉사료
                .append("#")
                .append(kisvanSpec.outJanAmt) // 잔액
                .append("#")
                .append(kisvanSpec.outAccepterCode) // 매입사 코드
                .append("#")
                .append(kisvanSpec.outAccepterName) // 매입사 명
                .append("#")
                .append(kisvanSpec.outVanKey) // 거래 고유 번호(취소 시 사용가 능)  // Van 키
                .append("#")
                .append(kisvanSpec.outPayType) // 결제 유형을 나타내는 변수
                .append("#")
                .append(kisvanSpec.outPayGubun) // 결제 구분을 나타내는 변수
                .append("#")
                .append(kisvanSpec.outUserID)
                .append("#")
                .append(kisvanSpec.outOrderNo)
                .append("#")
                .append(kisvanSpec.outMemberShipBarcodeNumber) // 멤버십 바코드 번호
                .append("#")
                .append(kisvanSpec.outOTC) // OTC
                .append("#")
                .append(kisvanSpec.outPosSerialNo) // POS 일련번호
                .append("#")
                .append(kisvanSpec.outCatId) // Cat ID
                .append("#")
                .append(kisvanSpec.outInstallMent)
                .append("#")
                .append(kisvanSpec.outTradeNumber) // 거래 번호
                .append("#")
                .append(kisvanSpec.outAccountNumber)
                .append("#")
                .append(kisvanSpec.outBarcodeNumber)
                .append("#")
                .append(kisvanSpec.outDiscountPoint)
                .append("#")
                .append(kisvanSpec.outUsedPoint)
                .append("#")
                .append(kisvanSpec.outStatusICCard);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("divIndex: " + divIndex);
        System.out.println("outReplyCode: " + kisvanSpec.outReplyCode);
        System.out.println("outReplyMsg1: " + kisvanSpec.outReplyMsg1);
        System.out.println("outReplyMsg2: " + kisvanSpec.outReplyMsg2);
        System.out.println("outTotAmt: " + kisvanSpec.outTotAmt);
        System.out.println("outVatAmt: " + kisvanSpec.outVatAmt);
        System.out.println("outAuthNo: " + kisvanSpec.outAuthNo);
        System.out.println("outAuthDate: " + kisvanSpec.outAuthDate);
        System.out.println("outIssuerName: " + kisvanSpec.outIssuerName);
        System.out.println("outCardNo: " + kisvanSpec.outCardNo);
        System.out.println("outMerchantRegNo: " + kisvanSpec.outMerchantRegNo);
        System.out.println("outIssuerCode: " + kisvanSpec.outIssuerCode);
        System.out.println("outWCC: " + kisvanSpec.outWCC);
        System.out.println("outCardBrand: " + kisvanSpec.outCardBrand);
        System.out.println("outSignYn: " + kisvanSpec.outSignYn);
        System.out.println("outSvcAmt: " + kisvanSpec.outSvcAmt);
        System.out.println("outJanAmt: " + kisvanSpec.outJanAmt);
        System.out.println("outAccepterCode: " + kisvanSpec.outAccepterCode);
        System.out.println("outAccepterName: " + kisvanSpec.outAccepterName);
        System.out.println("outVanKey: " + kisvanSpec.outVanKey);
        System.out.println("outPayType: " + kisvanSpec.outPayType);
        System.out.println("outPayGubun: " + kisvanSpec.outPayGubun);
        System.out.println("outUserID: " + kisvanSpec.outUserID);
        System.out.println("outOrderNo: " + kisvanSpec.outOrderNo);
        System.out.println("outMemberShipBarcodeNumber: " + kisvanSpec.outMemberShipBarcodeNumber);
        System.out.println("outOTC: " + kisvanSpec.outOTC);
        System.out.println("outPosSerialNo: " + kisvanSpec.outPosSerialNo);
        System.out.println("outCatId: " + kisvanSpec.outCatId);
        System.out.println("outInstallMent: " + kisvanSpec.outInstallMent);
        System.out.println("outTradeNumber: " + kisvanSpec.outTradeNumber);
        System.out.println("outAccountNumber: " + kisvanSpec.outAccountNumber);
        System.out.println("outBarcodeNumber: " + kisvanSpec.outBarcodeNumber);
        System.out.println("outDiscountPoint: " + kisvanSpec.outDiscountPoint);
        System.out.println("outUsedPoint: " + kisvanSpec.outUsedPoint);
        System.out.println("outStatusICCard: " + kisvanSpec.outStatusICCard);

        String text = sb.toString();
        String[] words = text.split("#"); // #를 제외한 단어들을 배열로 추출
        JSONObject jsonObject = new JSONObject();
        int k = 0;
        for (int i = 0; i < words.length; i++) {
            String value = words[i];
            if (!value.equals("null") && !value.isEmpty()) {
                String key = "response" + k;
                try {
                    jsonObject.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                k++;  // k 변수를 1씩 증가시킴
            }
        }
        handleSendMain(jsonObject);
        qrAndCodeInit();
    }

    void qrAndCodeInit() {
        bRunOnce = false;
        qrSizeCheck = false;
        runAfter3Seconds = false;
        codeAndQR= false;
        qrAndBardCodeData = "";
    }

    private void payCancel(String money, String tax, String tCode, String aNumber, String aDate) {
        //String OrgAuthDate = "20230531";
        intent = new Intent(appName); // 앱 이름을 가지고 Intent 객체를 생성합니다.
        kisvanSpec.Init(); // KisvanSpec 객체의 Init() 메서드를 호출하여 초기화 작업을 수행합니다.
        intent.putExtra("inDefaultCatId", true); // Intent에 "inDefaultCatId"라는 키로 true 값을 추가합니다.
        kisvanSpec.inTestMode = "Y"; // 테스트 모드를 활성화하기 위해 "Y" 값을 할당합니다.
        // 빈 값으로 입력하면 AndroidAgent 내에서 선택된 가맹점으로 결제가 진행됩니다.
        kisvanSpec.inTranCode = tCode; // 거래 코드를 설정합니다.
        kisvanSpec.inTotAmt = String.valueOf(Integer.parseInt(money) + Integer.parseInt(tax)); // 총 금액을 설정합니다.
        kisvanSpec.inVatAmt = tax; // 부가세 금액을 설정합니다.
        kisvanSpec.inOrgAuthDate = aDate.substring(2,8);
        kisvanSpec.inOrgAuthNo = aNumber;
        kisvanSpec.RequestData(intent); // KisvanSpec 객체의 RequestData() 메서드를 호출하여 intent와 관련된 데이터를 설정합니다.
        intent.putExtra("ResultType", 1); // Intent에 "ResultType"이라는 키로 1 값을 추가합니다. (0 : Activity, 1 : BroadCastReceiver)
        IntentFilter intentFilter = new IntentFilter(); // IntentFilter 객체를 생성합니다.
        intentFilter.addAction(appName); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
        rPayment = new AgentResPayment(); // AndroidAgentRes 클래스의 인스턴스를 생성하여 receiver 변수에 할당합니다.
        registerReceiver(rPayment, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // 액티비티를 시작할 때 애니메이션 효과를 비활성화
        startActivity(intent); // Intent를 사용하여 액티비티를 시작합니다.
    }


    private void easyPayCancel() {
        Log.e("last", " : " + qrAndBardCodeData);
        intent = new Intent(appName);
        kisvanSpec.Init();
        kisvanSpec.inTestMode = "N";
        intent.putExtra("inFullScreenFlag", false);
        kisvanSpec.inTotAmt = "1004";
        kisvanSpec.inVatAmt = "91";
        kisvanSpec.inTranCode = "RR";
        kisvanSpec.inWCC = "K";
        kisvanSpec.inOrgAuthDate = "20230531";
        kisvanSpec.inOrgAuthNo = "53660744";
        kisvanSpec.inBarcodeNumber = String.format("%03d", qrAndBardCodeData.length()) + qrAndBardCodeData;
        kisvanSpec.RequestData(intent);
        intent.putExtra("ResultType", 1);
        IntentFilter intentFilter = new IntentFilter(); // IntentFilter 객체를 생성합니다.
        intentFilter.addAction(appName); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
        rEasyPayment = new AgentResEasyPayment(); // AndroidAgentRes 클래스의 인스턴스를 생성하여 receiver 변수에 할당합니다.
        registerReceiver(rEasyPayment, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
        startActivity(intent);
    }

    private void UndoContinuous() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isStarting = false;
                Log.e("연속 실행 취소" , " : " +  isStarting);
            }
        }, 1000);
    }
}














