package com.softrain.mypay;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.softrain.mypay.XprinterUtils.HexUtils;

import net.posprinter.IDeviceConnection;
import net.posprinter.POSConnect;
import net.posprinter.POSConst;
import net.posprinter.POSPrinter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class MyPayService extends Service implements Runnable {

    // BroadcastReceiver 객체로, mydisplay에서 데이터를 수신할 때 사용
    private final BroadcastReceiver bMdCommand ,kisReceiver;
    // 유형(type)
    private String mCurrType = null;
    // 함수명(function)
    private String mCurrFuncNm = null;
    // (name)
    private String mCurrData = null;
    // KisvanSpec 객체로, Kisvan 관련 스펙을 저장하고 사용
    KisvanSpec kisvanSpec;
    // 문자열 변수로, 인덱스를 저장
    String divIndex = null;
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
    private Handler mHandler = null;
    private int mTickTock = 0;
    // 파일 이름
    String gFileName = null;
    boolean startCheck;
    // POS 프린터 연결을 위한 IDeviceConnection 객체
    private IDeviceConnection connect;
    // POS 프린터 객체
    private POSPrinter printer;

    // mydisplay에서 결제 받아오는 곳
    public MyPayService() {
        bMdCommand = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(isStarting) {
                    return;
                }
                isStarting = true;
                payHandleRequest(intent);
            }
        };

        // kis에서 응답 값 받아오는 곳
        kisReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                PaymentResponse(intent);
            }
        };
    }

    @Override
    public synchronized void onCreate() {
        super.onCreate();
        //PrintFactory.init(this);         // PrintFactory 클래스를 초기화 (키오스크 영수증 출력 기기)
        TextLog.init(this);          // 로그 초기화
        POSConnect.init(this);  // 영수증 라이브러리 초기화

        kisvanSpec = new KisvanSpec();    // KisvanSpec 클래스의 인스턴스를 생성하여 kisvanSpec 변수에 할당
        SetSerialPort(SERIAL_PORT_NAME);  // 사용하여 시리얼 포트를 설정
        StartRxThread();                  // 수신 쓰레드를 시작
        isStarting = false;               // 연속 결제 방지 변수 설정
        connect("MAC");              // 프린트 연결
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

        // kis Intent필터 객체생성
        IntentFilter kisIntentFilter = new IntentFilter();
        kisIntentFilter.addAction(ConstDef.ACTION_NAME_FROM_KIS);
        registerReceiver(kisReceiver, kisIntentFilter);

        mHandler = new Handler();
        mHandler.postDelayed(this, 1000 /* milliseconds */);

        return START_STICKY; // START_STICKY 값을 반환하여 시스템이 서비스를 종료한 후에도 자동으로 서비스를 재시작하도록 지정
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // mydisplay에서 결제 데이터값 받는 곳
    private void payHandleRequest(Intent intent){
        mCurrType = intent.getStringExtra("type");
        mCurrFuncNm = intent.getStringExtra("funcNm");
        mCurrData = intent.getStringExtra("data");
        TextLog.o(" " + ConstDef.MYDISPLAY_TO_MYPAY_TAG + " " + mCurrType + " " + mCurrFuncNm + " " + mCurrData);
        switch (mCurrFuncNm) {
            // 결제
            case "$creditApproval":
                creditApproval(mCurrData);
                break;
            // 프린터
            case "$printReceipt":
                printReceipt(mCurrData);
                break;
            // 결제취소
            case "$cancelApproval":
                cancelApproval(mCurrData);
                break;
            // 로그 업로드
            case "$appUpdateReq":
                requestUpdateVersion();
                break;
            // apk 업데이트
            case "$appLogUploadReq":
                uploadLog(mCurrData);
                break;
            default:
                // mCurrFuncNm과 일치하는 값이 없는 경우에 수행할 동작을 작성합니다.
                // 예를 들면, 오류 처리를 할 수 있습니다.
                TextLog.o(" mCurrFuncNm error(switch) " + ConstDef.MYDISPLAY_TO_MYPAY_TAG + " " + mCurrType + " " + mCurrFuncNm + " " + mCurrData);
                break;
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
            String payReceiptType = jsonObject.optString("payReceiptType", null);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(mCurrType) " + mCurrType);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(mCurrFuncNm) " + mCurrFuncNm);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(mCurrData) " + mCurrData);
            Log.i(ConstDef.TAG," MyService: mydisplay로부터 값 받는 곳(rcode) " + tCode);
            TextLog.o(" " + ConstDef.MYDISPLAY_TO_MYPAY_GENERAL_PAYMENT_TAG + " " + mCurrType + " " + mCurrFuncNm + " " + mCurrData);
//            codeAndQR = true;
            // 카드결제
            if(tCode.equals("D1")) {
                // 결제창 띄우고 결제 처리 메소드
                requestData(money, tax, tCode);
            }
            // 간편결제
            else if(tCode.equals("AC")) {
                codeAndQR = true;
            }
            // 현금 영수증
            else if(tCode.equals("H1")) {
                cashReceipt(money, tax, tCode, payReceiptType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 결제창 띄우고 결제 처리
    public void requestData(String money, String tax, String tCode) {
        intent = new Intent(ConstDef.ACTION_NAME_FROM_KIS); // 앱 이름을 가지고 Intent 객체를 생성합니다.
        kisvanSpec.Init(); // KisvanSpec 객체의 Init() 메서드를 호출하여 초기화 작업을 수행합니다.
        kisvanSpec.inTestMode = "Y"; // 테스트 모드를 활성화하기 위해 "Y" 값을 할당합니다.
        // 빈 값으로 입력하면 AndroidAgent 내에서 선택된 가맹점으로 결제가 진행됩니다.
        kisvanSpec.inTranCode = tCode; // 거래 코드를 설정합니다.
        kisvanSpec.inTotAmt = String.valueOf(Integer.parseInt(money) + Integer.parseInt(tax));//String.valueOf(Integer.parseInt(money) + Integer.parseInt(tax))
        kisvanSpec.inVatAmt = tax; // 부가세 금액을 설정합니다.
        kisvanSpec.isSavePath = true; // 5만원 이상 시 싸인 저장
        kisvanSpec.RequestData(intent); // KisvanSpec 객체의 RequestData() 메서드를 호출하여 intent와 관련된 데이터를 설정합니다.
        intent.putExtra("ResultType", 1); // Intent에 "ResultType"이라는 키로 1 값을 추가합니다. (0 : Activity, 1 : BroadCastReceiver)
        IntentFilter intentFilter = new IntentFilter(); // IntentFilter 객체를 생성합니다.
        intentFilter.addAction(ConstDef.ACTION_NAME_FROM_KIS); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
        registerReceiver(kisReceiver, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent); // Intent를 사용하여 액티비티를 시작합니다.
        TextLog.o(" (card payment) " + ConstDef.KIS_PAYMENT_REQUEST_TAG + " inTestMode: " + kisvanSpec.inTestMode + " inTranCode: " + kisvanSpec.inTranCode + " inTotAmt: " + kisvanSpec.inTotAmt + " inVatAmt: " + kisvanSpec.inVatAmt + " isSavePath: " + kisvanSpec.isSavePath);
        //        rPayment = new AgentResPayment(); // AndroidAgentRes 클래스의 인스턴스를 생성하여 receiver 변수에 할당합니다.
        //        registerReceiver(rPayment, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
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
            TextLog.o(" " + ConstDef.MYDISPLAY_TO_MYPAY_PRINT_TAG + " " + mCurrType + " " + mCurrFuncNm + " " + mCurrData);
            XprintReceipt(rReceiptPageNum, rStoreInfo, rReceiptInfo, rCardInfo, rBill);
            //PrintFactory.funcPrintText(rReceiptPageNum, rStoreInfo, rReceiptInfo, rCardInfo, rBill);
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
            TextLog.o(" " + ConstDef.MYDISPLAY_TO_MYPAY_CANCEL_PAYMENT_TAG + " " + mCurrType + " " + mCurrFuncNm + " " + mCurrData);
            // 결제창 취소창 띄우고 결제취소 처리 메소드
            payCancel(money, tax, tCode, aNumber, aDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 현금영수증 처리
    public void cashReceipt(String money, String tax, String tCode, String payReceiptType) {
        intent = new Intent(ConstDef.ACTION_NAME_FROM_KIS); // 앱 이름을 가지고 Intent 객체를 생성합니다.
        kisvanSpec.Init(); // KisvanSpec 객체의 Init() 메서드를 호출하여 초기화 작업을 수행합니다.
        kisvanSpec.inTestMode = "Y"; // 테스트 모드를 활성화하기 위해 "Y" 값을 할당합니다.
        // 빈 값으로 입력하면 AndroidAgent 내에서 선택된 가맹점으로 결제가 진행됩니다.
        kisvanSpec.inTranCode = tCode; // 거래 코드를 설정합니다.
        kisvanSpec.inTotAmt = String.valueOf(Integer.parseInt(money) + Integer.parseInt(tax));//String.valueOf(Integer.parseInt(money) + Integer.parseInt(tax))
        kisvanSpec.inVatAmt = tax; // 부가세 금액을 설정합니다.
        kisvanSpec.inCashAuthType = payReceiptType;
        kisvanSpec.inWCC = "K";
        kisvanSpec.RequestData(intent); // KisvanSpec 객체의 RequestData() 메서드를 호출하여 intent와 관련된 데이터를 설정합니다.
        intent.putExtra("ResultType", 1); // Intent에 "ResultType"이라는 키로 1 값을 추가합니다. (0 : Activity, 1 : BroadCastReceiver)
        IntentFilter intentFilter = new IntentFilter(); // IntentFilter 객체를 생성합니다.
        intentFilter.addAction(ConstDef.ACTION_NAME_FROM_KIS); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
        registerReceiver(kisReceiver, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent); // Intent를 사용하여 액티비티를 시작합니다.
        TextLog.o(" (Cash receipt) " + ConstDef.KIS_PAYMENT_REQUEST_TAG + " inTestMode: " + kisvanSpec.inTestMode + " inTranCode: " + kisvanSpec.inTranCode + " inTotAmt: " + kisvanSpec.inTotAmt + " inVatAmt: " + kisvanSpec.inVatAmt + " inCashAuthType: " + kisvanSpec.inCashAuthType + " inWCC: " + kisvanSpec.inWCC);
    }

    private void payCancel(String money, String tax, String tCode, String aNumber, String aDate) {
        intent = new Intent(ConstDef.ACTION_NAME_FROM_KIS); // 앱 이름을 가지고 Intent 객체를 생성합니다.
        kisvanSpec.Init(); // KisvanSpec 객체의 Init() 메서드를 호출하여 초기화 작업을 수행합니다.
        kisvanSpec.inTestMode = "Y"; // 테스트 모드를 활성화하기 위해 "Y" 값을 할당합니다.
        // 빈 값으로 입력하면 AndroidAgent 내에서 선택된 가맹점으로 결제가 진행됩니다.
        kisvanSpec.inTranCode = tCode; // 거래 코드를 설정합니다.
        kisvanSpec.inTotAmt = String.valueOf(Integer.parseInt(money) + Integer.parseInt(tax)); // 총 금액을 설정합니다.
        kisvanSpec.inVatAmt = tax; // 부가세 금액을 설정합니다.
        kisvanSpec.inOrgAuthDate = aDate.substring(2,8);//
        kisvanSpec.inOrgAuthNo = aNumber;
        kisvanSpec.RequestData(intent); // KisvanSpec 객체의 RequestData() 메서드를 호출하여 intent와 관련된 데이터를 설정합니다.
        intent.putExtra("ResultType", 1); // Intent에 "ResultType"이라는 키로 1 값을 추가합니다. (0 : Activity, 1 : BroadCastReceiver)
        IntentFilter intentFilter = new IntentFilter(); // IntentFilter 객체를 생성합니다.
        intentFilter.addAction(ConstDef.ACTION_NAME_FROM_KIS); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
        registerReceiver(kisReceiver, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // 액티비티를 시작할 때 애니메이션 효과를 비활성화
        startActivity(intent); // Intent를 사용하여 액티비티를 시작합니다.
        TextLog.o(" (pay Cancel) " + ConstDef.KIS_PAYMENT_REQUEST_TAG + " inTestMode: " + kisvanSpec.inTestMode + " inTranCode: " + kisvanSpec.inTranCode + " inTotAmt: " + kisvanSpec.inTotAmt + " inVatAmt: " + kisvanSpec.inVatAmt + " inOrgAuthDate: " + kisvanSpec.inOrgAuthDate + " inOrgAuthNo: " + kisvanSpec.inOrgAuthNo);

    }

    // 카드 결제
    public void PaymentResponse(Intent intent) {
        if(intent == null) return; // Intent가 null인 경우 종료
        kisvanSpec.ResponseData(intent); // kisvanSpec 객체로부터 데이터 가져오기
        Log.e("@@@응답값 예외처리 값들 확인 ", " divIndex: "+divIndex+ " kisvanSpec.outAuthDate: "+kisvanSpec.outAuthDate);
        if(divIndex == null) {
            divIndex = "undefined";
        }
        StringBuilder sb = new StringBuilder();
        if (kisvanSpec.outAuthDate != null && kisvanSpec.outAuthDate.equals("")) {
            kisvanSpec.outAuthDate = CurrentDate();
        }
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
        System.out.println("kisvanSpec.outSignFilePath: " + kisvanSpec.outSignFilePath);

        TextLog.o(" " + ConstDef.KIS_PAYMENT_RESPONSE_TAG + " " + sb);
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
        TextLog.o(" " + ConstDef.MYPAYT_TO_MYDISPLAY_TAG + " " + ConstDef.ACTION_NAME_TO_MYDISPLAY + " " +ConstDef.CMD_TYPE + " " + ConstDef.CMD_FUNCNM_INTENT_RECEIVED + " " + jsonObject);
        UndoContinuous();
    }

    // 연속방지 초기화 메소드
    private void UndoContinuous() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isStarting = false;
            }
        }, 1000);
    }

    // 프린터 연결을 수행하는 메서드
    private void connect(String mac) {
        TextLog.o(" " + ConstDef.MYPAYT_PRINT_CONNECTION_STATUS + " Status: " + connect);
        if (connect != null) {
            connect.close();
        }
        connect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_USB);
        connect.connect(null, (code, msg) -> {
            if (code == POSConnect.CONNECT_SUCCESS) {
                // 연결 성공 시
                printer = new POSPrinter(connect);
                connect.startReadLoop(this::receiveData);
                TextLog.o(" " + ConstDef.MYPAYT_PRINT_CONNECTION_STATUS + " Success: " + connect);
            } else if (code == POSConnect.CONNECT_FAIL) {
                // 연결 실패 시
                TextLog.o(" " + ConstDef.MYPAYT_PRINT_CONNECTION_STATUS + " Failed: " + connect);
            } else if (code == POSConnect.CONNECT_INTERRUPT) {
                // 연결 중단 시
                TextLog.o(" " + ConstDef.MYPAYT_PRINT_CONNECTION_STATUS + " Interruption: " + connect);
            }
        });
    }
    // 데이터 수신 처리 메서드
    private void receiveData(byte[] data) {
        String hexString = HexUtils.bytes2HexStrWithSpace(data, data.length);
        // 사용할 코드로 처리하세요
        // (HexUtils.bytes2String(data))
    }
    private void XprintReceipt(String rReceiptPageNum, String rStore, String rReceipt, String rCard, String rBill) {
        if(printer != null) {
            TextLog.o(" " + ConstDef.MYPAYT_PRINT_OUTPUT_VALUES + " rReceiptPageNum: " + rReceiptPageNum + " rStore: " + rStore + " rReceipt: " + rReceipt + " rCard: " + rCard + " rBill: " + rBill + " outSignFilePath: " + kisvanSpec.outSignFilePath);
            try {
                // 매장정보
                JSONObject joStore = new JSONObject(rStore);
                String sName = joStore.getString("sName");
                String sAddress = joStore.getString("sAddress");
                String sRepresentative = joStore.getString("sRepresentative");
                String sTel = joStore.getString("sTel");
                String sOrderDate = joStore.getString("sOrderDate");
                String sOrderNumber = joStore.optString("sOrderNumber", null);

                printer.setCharSet("CP949");
                // 메뉴 교환권이 필요 시
                if(rReceiptPageNum.equals("2")){
                    printer.printText("[메뉴 교환권]\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, 2).feedLine();
                    String couponNumber = String.format("%s%s",
                            convert("교환권 번호", 13),
                            convert(": "+"0157", 1));
                    printer.printString(couponNumber).feedLine();
                    String couponDate = String.format("%s%s",
                            convert("일시", 13),
                            convert(": "+sOrderDate, 1));
                    printer.printString(couponDate).feedLine();
                    printer.printString("-----------------------------------------------").feedLine();
                    String couponCategories = String.format("%s%s",
                            convert("상품명", 40),
                            convert("수량", 5));
                    printer.printString(couponCategories).feedLine();
                    printer.printString("-----------------------------------------------").feedLine();
                    JSONArray jaCouponData = new JSONArray(rReceipt);
                    for (int i = 0; i < jaCouponData.length(); i++) {
                        JSONObject joCouponData = jaCouponData.getJSONObject(i);
                        // JSON 객체에서 필요한 값을 추출
                        String name = joCouponData.getString("name");
                        int quantity = joCouponData.getInt("quantity");
                        StringBuilder sbCoupon = new StringBuilder();
                        sbCoupon.append(convert(name, 42));
                        sbCoupon.append(convert(String.valueOf(quantity), 5));
                        printer.printString(sbCoupon.toString()).feedLine();
                    }
                    printer.printString("-----------------------------------------------").feedLine(5);
                    printer.cutPaper();
                }

                printer.initializePrinter().printText("[영수증]\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, 2).feedLine();
                String storeName = String.format("%s%s",
                        convert("매 장 명", 10),
                        convert(sName, 30));
                printer.printString(storeName).feedLine();

                String storeAddress = String.format("%s%s",
                        convert("매장주소", 10),
                        convert(sAddress, 30));
                printer.printString(storeAddress).feedLine();

                String storeRepresentative = String.format("%s%s",
                        convert("대표자", 10),
                        convert(sRepresentative, 30));
                printer.printString(storeRepresentative).feedLine();

                String storeRepresentativeNumber = String.format("%s%s",
                        convert("TEL", 10),
                        convert(sTel, 30));
                printer.printString(storeRepresentativeNumber).feedLine();

                String storeOrderDate = String.format("%s%s",
                        convert("주문일시", 10),
                        convert(sOrderDate, 30));
                printer.printString(storeOrderDate).feedLine();

                String storeOrderNumber = String.format("%s%s",
                        convert("룸번호", 10),
                        convert(sOrderNumber, 30));
                printer.printString(storeOrderNumber).feedLine();
                printer.printString("-----------------------------------------------").feedLine();

                String storeCategories = String.format("%s%s%s%s",
                        convert("상품명", 23),
                        convert("단가", 8),
                        convert("수량", 8),
                        convert("금액", 9));
                printer.printTextAttribute(storeCategories, POSConst.FNT_BOLD).printString("-----------------------------------------------").feedLine();

                JSONArray jaStoreData = new JSONArray(rReceipt);
                for (int i = 0; i < jaStoreData.length(); i++) {
                    JSONObject joStoreData = jaStoreData.getJSONObject(i);
                    // JSON 객체에서 필요한 값을 추출
                    String name = joStoreData.getString("name");
                    String price = joStoreData.getString("price");
                    int quantity = joStoreData.getInt("quantity");
                    int amount = joStoreData.getInt("quantity") * joStoreData.getInt("price");
                    StringBuilder sbStoreData = new StringBuilder();
                    sbStoreData.append(convert(name, 22));
                    sbStoreData.append(convert(formatPrice(Integer.parseInt(price)), 10));
                    sbStoreData.append(convert(String.valueOf(quantity), 7));
                    sbStoreData.append(convert(formatPrice(amount), 9));
                    printer.printString(sbStoreData.toString());
                }

                printer.printString("-----------------------------------------------").feedLine();

                // 돈 세금
                JSONObject joBill = new JSONObject(rBill);
                String sTotal = joBill.getString("total");
                String sTax = joBill.getString("tax");
                String storeMoney = String.format("%s%s",
                        convert("공급가액", 39),
                        convert(formatPrice(Integer.parseInt(sTotal)-Integer.parseInt(sTax)), 9));
                printer.printString(storeMoney);

                String storeSurtax = String.format("%s%s",
                        convert("부 가 세", 39),
                        convert(formatPrice(Integer.parseInt(sTax)), 9));
                printer.printString(storeSurtax).printString("-----------------------------------------------").feedLine();

                String storeResult = String.format("%s%s",
                        convert("합계", 39),
                        convert(formatPrice(Integer.parseInt(sTotal)), 9));
                printer.printString(storeResult).printString("-----------------------------------------------").feedLine();

                JSONArray jaCardInfo = new JSONArray(rCard);
                for (int i = 0; i < jaCardInfo.length(); i++) {
                    JSONObject joCardInfo = jaCardInfo.getJSONObject(i);
                    // JSON 객체에서 필요한 값을 추출
                    String rIssuingCompany = joCardInfo.getString("issuingCompany");
                    String rCardNumber = joCardInfo.getString("cardNumber");
                    String rApprovalNumber = joCardInfo.getString("approvalNumber");
                    String rApprovalDate = joCardInfo.getString("approvalDate");
                    String rMerchantNumber = joCardInfo.getString("merchantNumber");
                    String rApprovalMoney = joCardInfo.getString("approvalMoney");
                    if(rIssuingCompany.equals("cashReceipt")) {
                        printer.printText("<<< 현금영수증 >>>\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_1HEIGHT);

                        String cardNumber = String.format("%s%s",
                                convert2("식별번호  ", 8),
                                convert(" : "+rCardNumber, 30));
                        printer.printString(cardNumber).feedLine();

                        int total = Integer.parseInt(rApprovalMoney);
                        String approvalMoney = String.format("%s%s",
                                convert2("승인금액  ", 8),
                                convert(" : "+formatPrice(Integer.parseInt(rApprovalMoney)), 30));
                        printer.printString(approvalMoney).feedLine();

                        String approvalNumber = String.format("%s%s",
                                convert2("승인번호  ", 8),
                                convert(" : "+rApprovalNumber, 30));
                        printer.printString(approvalNumber).feedLine();

                        String approvalDate = String.format("%s%s",
                                convert2("승인일시  ", 8),
                                convert(" : "+rApprovalDate, 30));
                        printer.printString(approvalDate).feedLine();
                    }
                    else if(rIssuingCompany.equals("cash")) {

                    }
                    // 신용걸래
                    else {
                        printer.printText("<<< 신 용 승 인 정 보 >>>\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_1HEIGHT);
                        String cardCompanyName = String.format("%s%s",
                                convert2("카드종류  ", 8),
                                convert(" : "+rIssuingCompany, 30));
                        printer.printString(cardCompanyName).feedLine();

                        String cardNumber = String.format("%s%s",
                                convert2("카드번호  ", 8),
                                convert(" : "+rCardNumber, 30));
                        printer.printString(cardNumber).feedLine();

                        String installment = String.format("%s%s",
                                convert2("할부개월  " , 8),
                                convert(" : "+"일시불  ", 30));
                        printer.printString(installment).feedLine();

                        int total = Integer.parseInt(rApprovalMoney);
                        String approvalMoney = String.format("%s%s",
                                convert2("승인금액  ", 8),
                                convert(" : "+formatPrice(Integer.parseInt(rApprovalMoney)), 30));
                        printer.printString(approvalMoney).feedLine();

                        String approvalNumber = String.format("%s%s",
                                convert2("승인번호  ", 8),
                                convert(" : "+rApprovalNumber, 30));
                        printer.printString(approvalNumber).feedLine();

                        String approvalDate = String.format("%s%s",
                                convert2("승인일시  ", 8),
                                convert(" : "+rApprovalDate, 30));
                        printer.printString(approvalDate).feedLine();

                        String purchaseCompany = String.format("%s%s",
                                convert2("가맹점번호", 8),
                                convert(" : "+rMerchantNumber, 30));
                        printer.printString(purchaseCompany).feedLine();
                    }

                }
                // 파일 경로를 문자열로 저장합니다.
                String filePath = kisvanSpec.outSignFilePath;
                if(filePath != null) {
                    // File 객체를 생성합니다.
                    File file = new File(filePath);
                    // 파일이 존재하는지 확인합니다.
                    if (file.exists()) {
                        printer.printString("-----------------------------------------------").feedLine();
                        printer.printString("(서명)").feedLine(1);
                        printer.initializePrinter().printBitmap(filePath, POSConst.ALIGNMENT_CENTER, 300);
                        printer.feedLine(2);
                        kisvanSpec.outSignFilePath = "";
                    }
                }
                printer.printString("-----------------------------------------------").feedLine(5).cutPaper();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    // 한국 돈 표기법으로 형식화된 문자열 반환
    public static String formatPrice(int price) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        return decimalFormat.format(price);
    }

    // 전각문자의 개수만큼 문자열 길이를 빼주는 메서드
    public static String convert(String word, int size) {
        String formatter = String.format("%%-%ds", size - getKorCnt(word));
        return String.format(formatter, word);
    }

    // 전각문자의 개수만큼 문자열 길이를 빼주는 메서드
    public static String convert2(String word, int size) {
        String formatter = String.format("%%%ds", size - getKorCnt(word));
        return String.format(formatter, word);
    }

    // 전각문자 개수를 세주는 메서드
    private static int getKorCnt(String kor) {
        int cnt = 0;
        for (int i = 0; i < kor.length(); i++) {
            if (kor.charAt(i) >= '가' && kor.charAt(i) <= '힣') {
                cnt++;
            }
        }
        return cnt;
    }

    // 로그 업로드, app 업데이트
    @Override
    public void run() {

        if (mTickTock % 59 == 0) { /* every about 1 minute */
            showNotification();

            Date now = new Date();
            SimpleDateFormat formatter_HHmm = new SimpleDateFormat("HH:mm");
            String now_HHmm = formatter_HHmm.format(now);
            TextLog.o("run(), now = " + now_HHmm);

            if (now_HHmm.equals(ConstDef.UPDATE_TIME1) || now_HHmm.equals(ConstDef.UPDATE_TIME2)) {
                requestUpdateVersion();
            }
//            // 시간설정 테스트
//            else if (now_HHmm.equals("12:41")) {
//                requestUpdateVersion();
//            }
        }
        mTickTock++;
        // 테스트 로그 TextLog.o("mTickTock = " + mTickTock);
        mHandler.postDelayed(this, 1000 /* milliseconds */);
    }

    private void uploadLog(String mCurrData) {
        TextLog.o("uploadLog");
        getFileName();
        String ArgHttpMethod = "POST";
        String ArgHttpUrl = ConstDef.UPLOAD_URL_LOG + "?deviceId=" + mCurrData + "&subPath=mypay/log"+gFileName;
        String ArgHttpBody = getExternalFilesDir(null).getAbsolutePath() + "/mypay_log.txt";
        HTTPModuleClass http = new HTTPModuleClass(this);
        http.execute(Integer.toString(ConstDef.HTTP_POST_UPLOAD_LOG), ArgHttpMethod, ArgHttpUrl, ArgHttpBody, ConstDef.HTTP_TYPE_FILE);
    }

    private void requestUpdateVersion() {
        TextLog.o("requestUpdateVersion");
        String ArgHttpMethod = "GET";
        String ArgHttpUrl = ConstDef.UPDATE_URL_VERSION;
        String ArgHttpBody = "";
        HTTPModuleClass http = new HTTPModuleClass(this);
        http.execute(Integer.toString(ConstDef.HTTP_GET_UPDATE_VERSION), ArgHttpMethod, ArgHttpUrl, ArgHttpBody, ConstDef.HTTP_TYPE_DATA);
    }

    private void requestUpdateApk() {
        TextLog.o("requestUpdateApk");
        String ArgHttpMethod = "GET";
        String ArgHttpUrl = ConstDef.UPDATE_URL_APK;
        String ArgHttpBody = "";
        HTTPModuleClass http = new HTTPModuleClass(this);
        http.execute(Integer.toString(ConstDef.HTTP_GET_UPDATE_APK), ArgHttpMethod, ArgHttpUrl, ArgHttpBody, ConstDef.HTTP_TYPE_FILE);
    }

    private class HTTPModuleClass extends HTTPBaseClass {
        public HTTPModuleClass(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(Integer values) {
            int request_code = values.intValue();

            TextLog.o("[Resp] RequestCode = " + request_code);

            if (request_code == ConstDef.HTTP_GET_UPDATE_VERSION) {
                handleHttpGetUpdateVersion(RetResult, RetHttpStatusCode, RetHttpBody);
            }
            else if (request_code == ConstDef.HTTP_GET_UPDATE_APK) {
                handleHttpGetUpdateApk(RetResult, RetHttpStatusCode, RetHttpBody);
            }
            else if (request_code == ConstDef.HTTP_POST_UPLOAD_LOG) {
                handleHttpPostUploadLog(RetResult, RetHttpStatusCode, RetHttpBody);
            }
        }
    }

    private void handleHttpGetUpdateVersion(String ReturnResult, String ReturnHttpStatusCode, String ReturnHttpBody) {
        if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_OK)) {
            TextLog.o("handleHttpGetUpdateVersion: OK");
            TextLog.o("handleHttpGetUpdateVersion: |" + ReturnHttpStatusCode + "|");
            TextLog.o("handleHttpGetUpdateVersion: |" + ReturnHttpBody + "|");

            int currVersionCode, postVersionCode;

            currVersionCode = ConstFunc.getAppVersionCode(this);

            /*
             * {
             *     "versionCode" : 3,
             *     "versionName" : "3.0"
             * }
             */

            try {
                JSONObject jsonObj = new JSONObject(ReturnHttpBody);
                postVersionCode = jsonObj.getInt("versionCode");
            }
            catch (JSONException e) {
                postVersionCode = 0;
            }

            TextLog.o("handleHttpGetUpdateVersion: [curr] VersionCode = " + currVersionCode);
            TextLog.o("handleHttpGetUpdateVersion: [post] VersionCode = " + postVersionCode);

            if (postVersionCode > currVersionCode) {
                requestUpdateApk();
            }
        }
        else if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_NETWORK_DISCONNECTED)) {
            TextLog.o("[error] handleHttpGetUpdateVersion: NETWORK_DISCONNECTED");
        }
        else if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_SERVER_ERROR_RESPONSE)) {
            TextLog.o("[error] handleHttpGetUpdateVersion: SERVER_ERROR_RESPONSE");
        }
        else if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_SERVER_TIMEOUT)) {
            TextLog.o("[error] handleHttpGetUpdateVersion: SERVER_TIMEOUT");
        }
        else {
            TextLog.o("[error] handleHttpGetUpdateVersion: UNKNOWN_ERROR");
        }
    }

    private void handleHttpGetUpdateApk(String ReturnResult, String ReturnHttpStatusCode, String ReturnHttpBody) {
        if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_OK)) {
            TextLog.o("handleHttpGetUpdateApk: OK");
            TextLog.o("handleHttpGetUpdateApk: |" + ReturnHttpStatusCode + "|");
            TextLog.o("handleHttpGetUpdateApk: |" + ReturnHttpBody + "|");

            final PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(ReturnHttpBody /* apk_full_path */, 0);

            int currVersionCode, postVersionCode;

            currVersionCode = ConstFunc.getAppVersionCode(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                postVersionCode = (int)info.getLongVersionCode();
            }
            else {
                postVersionCode = info.versionCode;
            }

            TextLog.o("handleHttpGetUpdateApk: [curr] VersionName = " + ConstFunc.getAppVersionName(this) + ", VersionCode = " + currVersionCode);
            TextLog.o("handleHttpGetUpdateApk: [post] VersionName = " + info.versionName + ", VersionCode = " + postVersionCode);

            if (postVersionCode > currVersionCode) {
                TextLog.o("handleHttpGetUpdateApk: Update Start");

                AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);

                Date now = new Date();
                now.setTime(java.lang.System.currentTimeMillis() + ConstDef.ALARM_TIME);
                am.set(AlarmManager.RTC_WAKEUP, now.getTime(), pi);

                TextLog.o("handleHttpGetUpdateApk: Restart Application in " + ConstDef.ALARM_TIME + " ms");

                ConstFunc.installApk(this, ReturnHttpBody /* apk_full_path */);
            }
        }
        else if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_NETWORK_DISCONNECTED)) {
            TextLog.o("[error] handleHttpGetUpdateApk: NETWORK_DISCONNECTED");
        }
        else if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_SERVER_ERROR_RESPONSE)) {
            TextLog.o("[error] handleHttpGetUpdateApk: SERVER_ERROR_RESPONSE");
        }
        else if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_SERVER_TIMEOUT)) {
            TextLog.o("[error] handleHttpGetUpdateApk: SERVER_TIMEOUT");
        }
        else {
            TextLog.o("[error] handleHttpGetUpdateApk: UNKNOWN_ERROR");
        }
    }

    private void handleHttpPostUploadLog(String ReturnResult, String ReturnHttpStatusCode, String ReturnHttpBody) {
        if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_OK)) {
            TextLog.o("handleHttpPostUploadLog: OK");
            TextLog.o("handleHttpPostUploadLog: |" + ReturnHttpStatusCode + "|");
            TextLog.o("handleHttpPostUploadLog: |" + ReturnHttpBody + "|");
        }
        else if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_NETWORK_DISCONNECTED)) {
            TextLog.o("[error] handleHttpPostUploadLog: NETWORK_DISCONNECTED");
        }
        else if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_SERVER_ERROR_RESPONSE)) {
            TextLog.o("[error] handleHttpPostUploadLog: SERVER_ERROR_RESPONSE");
        }
        else if (ReturnResult.equals(ConstDef.RETURN_RESULT_VALUE_SERVER_TIMEOUT)) {
            TextLog.o("[error] handleHttpPostUploadLog: SERVER_TIMEOUT");
        }
        else {
            TextLog.o("[error] handleHttpPostUploadLog: UNKNOWN_ERROR");
        }
    }

    //  앱이 백그라운드에서 실행되는 동안 알림을 유지하고자 할 때 사용
    @SuppressLint("NewApi")  // .setColor(Color.BLACK)에 관한 경고를 무시하도록 지정. NewApi는 여기서 최신 버전의 안드로이드 API를 가리킴
    private void showNotification() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formatedNow = formatter.format(now);
        String content_title = "mypay 앱 실행중 [버전: " + ConstFunc.getAppVersionName(this) + " (" + ConstFunc.getAppVersionCode(this) + ")]";
        String content_text = "[" + formatedNow + "]";
        int notification_id = 1;
        String channel_id = "default";
        TextLog.o("Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT + ", Build.VERSION_CODES.O = " + Build.VERSION_CODES.O);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();

        Notification notification = new Notification.Builder(this)
                .setContentTitle(content_title)
                .setContentText(content_text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setColor(Color.BLACK)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR;

        notificationManager.notify(notification_id, notification);
    }

    // todo public native int installApk(String apk);
    public void getFileName() {
        Date currentDate = new Date(); // 현재 시간을 나타내는 Date 객체
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss"); // 포맷 형식은 "yyyy-MM-dd_HH:mm:ss"로 지정되어 있으며, 이는 연도-월-일_시간:분:초 형식을 나타냄
        String sFormattedDate = simpleDateFormat.format(currentDate);
        String fileName = "log_" + sFormattedDate + ".txt";
        System.out.println("fileName: " + fileName);
        gFileName = fileName;
    }

    // 현제 년,월,일 반환 메소드
    private String CurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR); // 현재 년도
        int month = calendar.get(Calendar.MONTH) + 1; // 현재 월 (0부터 시작하므로 1을 더해줌)
        int day = calendar.get(Calendar.DAY_OF_MONTH); // 현재 일
        DecimalFormat decimalFormat = new DecimalFormat("00");
        String formattedMonth = decimalFormat.format(month);
        String formattedDay = decimalFormat.format(day);
        String payDate = ""+year + formattedMonth + formattedDay;
        return payDate;
    }

    // 간편결제
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
                        int dataSize = 0;
                        byte bufferData[] = new byte[128];

                        byte[] rbufferData = receivedData.getBytes("UTF-8"); // UTF-8   CP949
                        for (int j = 0; j < rbufferData.length; j++) {
                            if (rbufferData[j] >= 0x20 && rbufferData[j] <= 0x7e) {
                                bufferData[dataSize++] = rbufferData[j];
                            }
                        }
                        String converted = new String(bufferData, 0, dataSize);
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
        if(size != 32) {
            qrSizeCheck = true;
        }
        String QuotesRemovalVaule = removeQuotes(buffer); // "제거하는 메소드
        qrAndBardCodeData += QuotesRemovalVaule;
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

    // todo 간편결제 들어가면 작업해야됨
    void EasyPaymentResponse(String setdata) {
        Intent intent = new Intent(ConstDef.ACTION_NAME_FROM_KIS);
        kisvanSpec.Init();
        kisvanSpec.inTestMode = "Y";
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
        intentFilter.addAction(ConstDef.ACTION_NAME_FROM_KIS); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
        rEasyPayment = new AgentResEasyPayment(); // AndroidAgentRes 클래스의 인스턴스를 생성하여 receiver 변수에 할당합니다.
        registerReceiver(rEasyPayment, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
        startActivity(intent);
    }

    // 간편결제 취소
    private void easyPayCancel() {
        intent = new Intent(ConstDef.ACTION_NAME_FROM_KIS);
        kisvanSpec.Init();
        kisvanSpec.inTestMode = "Y";
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
        intentFilter.addAction(ConstDef.ACTION_NAME_FROM_KIS); // intentFilter에 앱 이름에 해당하는 액션을 추가합니다.
        rEasyPayment = new AgentResEasyPayment(); // AndroidAgentRes 클래스의 인스턴스를 생성하여 receiver 변수에 할당합니다.
        registerReceiver(rEasyPayment, intentFilter); // receiver를 등록하여 액션에 대한 브로드캐스트 수신을 처리합니다.
        startActivity(intent);
    }

    public void PosResponse2(Intent intent) {
        if(intent == null) return; // Intent가 null인 경우 종료
        kisvanSpec.ResponseData(intent); // kisvanSpec 객체로부터 데이터 가져오기
        Log.e("@@@응답값 예외처리 값들 확인", " (간편결제) divIndex: "+divIndex+ " kisvanSpec.outAuthDate: "+kisvanSpec.outAuthDate);
        if(divIndex == null) {
            divIndex = "undefined";
        }
        StringBuilder sb = new StringBuilder();
        if (kisvanSpec.outAuthDate != null && kisvanSpec.outAuthDate.equals("")) {
            kisvanSpec.outAuthDate = CurrentDate();
        }
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

    // 간편결제 초기화 값
    void qrAndCodeInit() {
        bRunOnce = false;
        qrSizeCheck = false;
        runAfter3Seconds = false;
        codeAndQR= false;
        qrAndBardCodeData = "";
    }

    // 서비스 종료
    @Override
    public synchronized void onDestroy() {
        // 연결 끊기
        unregisterReceiver(bMdCommand);
        unregisterReceiver(kisReceiver);
        PrintFactory.printerDestroy();
        serialPort.close();
        connect.close();
        super.onDestroy();
    }

}

//    // 지불(payment)에 대한 응답(response)을 저장
//    AgentResPayment rPayment;
//// 방송 인텐트를 수신하는 역할
//public class AgentResPayment extends BroadcastReceiver {
//    // 방송을 수신하면 호출되는 콜백 메서드
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        // 신자를 등록 해제
//        unregisterReceiver(rPayment);
//        // 인텐트에 대한 응답을 처리
//        PaymentResponse(intent);
//    }
//}













