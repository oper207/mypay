package kr.co.softrain.mypaytest.sample;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import net.jt.pos.sdk.JTNetPosManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import kr.co.softrain.mypaytest.common.HexCode;
import kr.co.softrain.mypaytest.data.ability.BaseAbility;
import kr.co.softrain.mypaytest.data.ability.PlainNumAbility;
import kr.co.softrain.mypaytest.data.ability.SignAbility;
import kr.co.softrain.mypaytest.data.approval.CashApproval;
import kr.co.softrain.mypaytest.data.approval.CashIcApproval;
import kr.co.softrain.mypaytest.data.approval.CashIcInquiryApproval;
import kr.co.softrain.mypaytest.data.approval.CheckApproval;
import kr.co.softrain.mypaytest.data.approval.CreditApproval;
import kr.co.softrain.mypaytest.data.approval.KakaoInqApproval;
import kr.co.softrain.mypaytest.data.approval.MembershipApproval;
import kr.co.softrain.mypaytest.data.approval.PointApproval;
import kr.co.softrain.mypaytest.data.approval.QRApproval;
import kr.co.softrain.mypaytest.data.approval.StoreDownApproval;
import kr.co.softrain.mypaytest.data.approval.ZeroPayApproval;
import kr.co.softrain.mypaytest.data.spinner.AdditionalInfo;
import kr.co.softrain.mypaytest.data.spinner.DealGubun;
import kr.co.softrain.mypaytest.data.spinner.PointKind;
import kr.co.softrain.mypaytest.data.spinner.PointSaveGubun;
import kr.co.softrain.mypaytest.data.spinner.QRTransactionType;
import kr.co.softrain.mypaytest.data.spinner.QRWcc;
import kr.co.softrain.mypaytest.data.spinner.Signature;
import kr.co.softrain.mypaytest.data.spinner.TradeGubun;
import kr.co.softrain.mypaytest.data.spinner.Wcc;
import kr.co.softrain.mypaytest.data.spinner.ZeroPayGubun;
import kr.co.softrain.mypaytest.data.spinner.ZeroPayWCC;
import kr.co.softrain.mypaytest.data.spinner.code.ApprovalCode;
import kr.co.softrain.mypaytest.data.spinner.code.RequestCode;
import kr.co.softrain.mypaytest.utils.StringUtil;

public class MyPayService extends Service {

    private final BroadcastReceiver mMdCommand;
    private String mCurrType = null;
    private String mCurrFuncNm = null;
    private String mCurrData = null;
    private final String[] approvalCodes = ApprovalCode.getApprovalArr();
    private final String[] wccs = Wcc.getWccArr();
    private final String[] ZeroPaywccs = ZeroPayWCC.getZeroPayWCCArr();
    private final String[] signatures = Signature.getSignatureArr();
    private final String[] additionalInfos = AdditionalInfo.getAdditionalInfoArr();
    private final String[] dealGubuns = DealGubun.getDealGubunArr();
    private final String[] tradeGubuns = TradeGubun.getDealGubunArr();
    private final String[] pointSaveGubuns = PointSaveGubun.getPointSveGubunArr();
    private final String[] pointKindGubuns = PointKind.getPointKindGubunArr();
    private final String[] zeroPayGubuns = ZeroPayGubun.getZeroPayGubunArr();
    private final String[] zeroPayWCC = ZeroPayWCC.getZeroPayWCCArr();
    private final String[] qrTransactionType = QRTransactionType.getQRTransactionTypeArr();
    private final String[] qrWCC = QRWcc.getWccArr();

    private RequestCode requestCode;
    private JTNetPosManager.RequestCallback requestCallback;
    // 돈, 세금, TID
    private String money, tax, tid, rcode;
    // 미사용 변수 목록(할부, 봉사료)
    private String iMonths, svcCharge = "0";
    // 미사용 변수 목록(원거래일자 (취소시), 원승인번호 (취소시), 원거래고유번호 (취소시))
    private String drgDealDt, orgApprovalNo, orgUniqueNo = "";
    // todo
    String a = "0097";

    private String defaultApprovalCode = String.valueOf(ApprovalCode.getApproval(approvalCodes[0]));

    private String getValueFromJson(JSONObject joData, String sKey, String sDefault){
        try {
            return joData.getString(sKey);
        } catch (Exception e) {
            return sDefault;
        }
    }

    private void handleRequestMain(Intent intent){
        mCurrType = intent.getStringExtra("type");
        mCurrFuncNm = intent.getStringExtra("funcNm");
        mCurrData = intent.getStringExtra("data");
        try {
            JSONObject jsonObject = new JSONObject(mCurrData);
            money = jsonObject.getString("money");
            tax = jsonObject.getString("tax");
            tid = jsonObject.getString("tid");
            rcode = getValueFromJson(jsonObject,"rcode", defaultApprovalCode);
            requestCode = ApprovalCode.getApproval(rcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 결제창 띄우고 결제 처리 메소드
        requestTaskDaemon(requestCode);
    }

    private void handleKeyChange(){
        requestCode = ApprovalCode.getApproval("KC");
        // 결제창 띄우고 결제 처리 메소드
        requestTaskDaemon(requestCode);
    }

    public MyPayService() {

        // mydisplay에서 받아오는 곳
        mMdCommand = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleRequestMain(intent);
            }
        };

        // 결제 실행시 응답값 받는 곳
        requestCallback = new JTNetPosManager.RequestCallback() {
            @Override
            public void onResponse(Message msg) {
                byte[] response = msg.getData().getByteArray("RESPONSE_MSG");
                //Log.e("onResponse1", "[응답2] : " + response.length + "||" + response[response.length - 1]);
                if (response != null) {
                    String strResData = StringUtil.byteArrayToString(response);

                    String text = a+" "+strResData+" "+money+" "+tax;
                    String[] words = text.split("\\s+"); // 띄어쓰기를 제외한 단어들을 배열로 추출
                    JSONObject jsonObject = new JSONObject();
                    for (int i = 0; i < words.length; i++) {

                        String key = "response" + i;
                        String value = words[i];
                        try {
                                jsonObject.put(key, value);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("조건문 전 값", " : " + words[0]);

                    if(words[0].equals("0097키")) {
                        handleKeyChange();
                        // todo
                        a = "keychange";
                    } else {
                        Log.e("onResponse3", " : " +  jsonObject.toString());
                        Intent intent = new Intent("softrain.intent.action.rpay");
                        intent.putExtra("type", "P_ModuleFunc");
                        intent.putExtra("funcNm", "$creditApproval");
                        intent.putExtra("data", jsonObject.toString());
                        sendBroadcast(intent);
                    }

                }
            }
        };
    }

    @Override
    public synchronized void onCreate() {
        super.onCreate();

        //데몬 연동 객체 생성 및 서비스 연동 함수
        JTNetPosManager.init(getApplicationContext());
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        showNotification();
        // intent필터 객체생성
        IntentFilter filterMdCommand = new IntentFilter();
        filterMdCommand.addAction("softrain.intent.action.pay");
        registerReceiver(mMdCommand, filterMdCommand);

        return START_STICKY;
    }

    @Override
    public synchronized void onDestroy() {
        // 연결 끊기
        JTNetPosManager.getInstance().destroy(getApplicationContext());
        unregisterReceiver(mMdCommand);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("NewApi")
    private void showNotification() {

        int notification_id = 1;
        String channel_id = "default";


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_id, "기본 채널", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            stopForeground(true);

            Notification notification = new NotificationCompat.Builder(this, channel_id)
                    .setContentTitle("title")
                    .setContentText("content")
                    .setSmallIcon(R.mipmap.ic_launcher)
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
                    .setContentTitle("title")
                    .setContentText("text")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(false)
                    .setWhen(System.currentTimeMillis())
                    .setColor(Color.BLACK)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .build();

            notification.flags |= Notification.FLAG_NO_CLEAR;

            notificationManager.notify(notification_id, notification);
        }
    }

    // 결제창 띄우고 결제 처리 메소드
    private void requestTaskDaemon(RequestCode requestCode) {

        byte[] requestArr = new byte[0];
        int requestCodeNum = 0;

        try {
            requestCodeNum = ((ApprovalCode) requestCode).getRequestCode();
            requestArr = getRequestByteArr(requestCode);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return;
        }
        // 데몬 연동 객체 리턴 함수
        // requestArr바이트 배열을 사용하여 서버로 요청을 전송하고 응답을 수신하면
        // requestCallback 객체의 onResponse()메서드가 응답 데이터와 함께 호출한다.
        JTNetPosManager.getInstance().jtdmProcess(requestCodeNum, requestArr, requestCallback);
    }


    public byte[] getRequestByteArr(RequestCode requestCode) throws ClassCastException  {
        byte[] requestArr = new byte[0];

        ApprovalCode approvalCode = ((ApprovalCode) requestCode);

        byte[] codeBytes = approvalCode.getCode().getBytes();

        switch (approvalCode) {
            case CREDIT_START:
            case CREDIT_BL:
            case CREDIT_CANCEL:
                requestArr = createCreditArr(approvalCode);
                break;
            case CASH_START:
            case CASH_CANCEL:
                requestArr = createCashArr(approvalCode);
                break;
            case MEMBERSHIP_START:
            case MEMBERSHIP_CANCEL:
            case MEMBERSHIP_INQUIRY:
                requestArr = createMembershipArr(approvalCode);
                break;
            case CASH_IC_START:
            case CASH_IC_CANCEL:
                requestArr = createCashIcArr(approvalCode);
                break;
            case CASH_IC_INQUIRY:
                requestArr = createCashIcInquiryArr(approvalCode);
                break;
            case POINT_USE:
            case POINT_USE_CANCEL:
            case POINT_INQUIRY:
            case POINT_SAVE:
            case POINT_SAVE_CANCEL:
                requestArr = createPointArr(approvalCode);
                break;
            case CHECK_INQUIRY:
                requestArr = createCheckInquiryArr(approvalCode);
                break;
            case STORE_DOWN:
                requestArr = createStoreDownArr(approvalCode);
                break;

            case ZEROPAY_AUTH:
            case ZEROPAY_CANCEL:
            case ZEROPAY_CHECK:
                requestArr = createZeroPayArr(approvalCode);
                break;
            case QRCODE_AUTH:
                requestArr = createQRAuthArrArr(approvalCode);
                break;
            case KAKAO_AUTH_INQUIRY:
            case KAKAO_CANCEL_INQUIRY:
                requestArr = createKakaopayArr(approvalCode);
                break;
            // todo
            case POS_NUM:
                requestArr = new PlainNumAbility(codeBytes).create();
                break;
            case KEY_EXCHANGE:
            case PRETRANSACTION:
            case CARD_CANCEL:
            case IC_CHECK:
                requestArr = new BaseAbility(codeBytes).create();
                break;
            case SIGN:
                byte[] amount = StringUtil.getLPadZero(9, money.trim()).getBytes();
                requestArr = new SignAbility(codeBytes, amount).create();
                break;
        }
        return requestArr;
    }


    /**
     * 신용 전문
     */
    private byte[] createCreditArr(ApprovalCode approvalCode) {

        byte[] code = approvalCode.getCode().getBytes();
        boolean isCancel = approvalCode == ApprovalCode.CREDIT_CANCEL;

        // 은련이면 은련코드
        if (isSelectUnionPay()) {
            code[3] = HexCode.UNION;
        }

        return new CreditApproval(
                code,
                getDeviceId(),
                getWCC(),
                getTrack2(isSelectKeyIn()),
                getIMonths(),
                getDealAmount(),
                getTax(),
                getSvcCharge(),
                getOrgDealDt(isCancel),
                getOrgApprovalNo(isCancel),
                getOrgUniqueNo(isCancel),
                getSignature()
        ).create();
    }

    /**
     * 현금영수증
     */
    private byte[] createCashArr(ApprovalCode approvalCode) {

        byte[] code = approvalCode.getCode().getBytes();
        boolean isCancel = approvalCode == ApprovalCode.CASH_CANCEL;

        return new CashApproval(
                code,
                getDeviceId(),
                getWCC(),
                getTrack2(isSelectKeyIn()),
                getDealAmount(),
                getTax(),
                getSvcCharge(),
                getDealGubun(),
                getOrgDealDt(isCancel),
                getOrgApprovalNo(isCancel)
        ).create();
    }

    /**
     * 수표조회
     */
    private byte[] createCheckInquiryArr(ApprovalCode approvalCode) {

        return new CheckApproval(
                approvalCode.getCode().getBytes(),
                getDeviceId(),
                getCheckNo(),
                getCheckAmount(),
                getCheckPublishDt(),
                getBankCode()
        ).create();
    }

    /**
     * 멤버쉽
     */
    private byte[] createMembershipArr(ApprovalCode approvalCode) {

        // todo 구현 되면 조건 수정 필요
        boolean isMembershipInquiry = approvalCode == ApprovalCode.MEMBERSHIP_INQUIRY;
        boolean isCancel = approvalCode == ApprovalCode.MEMBERSHIP_CANCEL;

        byte[] amount = !isMembershipInquiry ? getDealAmount() : StringUtil.getLPadZero(9, "").getBytes();


        return new MembershipApproval(
                approvalCode.getCode().getBytes(),
                getDeviceId(),
                getWCC(),
                getTrack2(isSelectKeyIn()),
                amount,
                getOrgDealDt(isCancel),
                getOrgApprovalNo(isCancel)
        ).create();
    }

    /**
     * 현금 IC
     */
    private byte[] createCashIcArr(ApprovalCode approvalCode) {

        // todo 구현 되면 조건 수정 필요
        boolean isCancel = approvalCode == ApprovalCode.CASH_IC_CANCEL;

        return new CashIcApproval(
                approvalCode.getCode().getBytes(),
                getDeviceId(),
                getDealAmount(),
                getTax(),
                getSvcCharge(),
                approvalCode == ApprovalCode.CASH_IC_CANCEL || approvalCode == ApprovalCode.CASH_IC_START || approvalCode == ApprovalCode.CASH_IC_INQUIRY ? getTradeGubun() : getDealGubun(),
                getOrgDealDt(isCancel),
                getOrgApprovalNo(isCancel)
        ).create();
    }

    /**
     * 현금 IC 체크
     */
    private byte[] createCashIcInquiryArr(ApprovalCode approvalCode) {
        return new CashIcInquiryApproval(
                approvalCode.getCode().getBytes(),
                getDeviceId()
        ).create();
    }

    /**
     * 거래구분
     */
    private byte[] getTradeGubun() {
        TradeGubun tradeGubun = TradeGubun.getDealGubun(tradeGubuns[0]); // 일반거래 or 간소화거래
        if (tradeGubun != null) {
            return tradeGubun.getCode().getBytes();
        }
        return "00".getBytes();
    }


    /**
     * 포인트
     */
    private byte[] createPointArr(ApprovalCode approvalCode) {

        return new PointApproval(
                approvalCode.getCode().getBytes(),
                getDeviceId(),
                getWCC(),
                getTrack2(isSelectKeyIn()),
                getPontSaveGubun(),
                getDealAmount(),
                getPointAmount(),
                getPontKindGubun(),
                getOrgDealDt(approvalCode == ApprovalCode.POINT_SAVE_CANCEL || approvalCode == ApprovalCode.POINT_USE_CANCEL ? true : false),
                getOrgApprovalNo(approvalCode == ApprovalCode.POINT_SAVE_CANCEL || approvalCode == ApprovalCode.POINT_USE_CANCEL ? true : false),
                getPasswordInfo()
        ).create();
    }

    /**
     * 가맹점 다운
     */
    private byte[] createStoreDownArr(ApprovalCode approvalCode) {
        return new StoreDownApproval(
                approvalCode.getCode().getBytes(),
                getDeviceId(),
                getBusinessNo()
        ).create();
    }

    /**
     * 제로페이
     */
    private byte[] createZeroPayArr(ApprovalCode approvalCode) {

        byte[] code = approvalCode.getCode().getBytes();
        boolean isCancel = approvalCode == ApprovalCode.ZEROPAY_CANCEL || approvalCode == ApprovalCode.ZEROPAY_CHECK;

        return new ZeroPayApproval(
                code,
                getDeviceId(),
                getZeroPayWCC(),
                getZeroPayData(),
                getZeroPayGubun(),
                StringUtil.getLPadZero(12, money.trim()).getBytes(),
                StringUtil.getLPadZero(12, tax.trim()).getBytes(),
                StringUtil.getLPadZero(12, "0".trim()).getBytes(),
                getOrgDealDt(isCancel),
                getOrgApprovalNo(isCancel),
                getZeroPayRandomData()
        ).create();
    }

    /**
     * 제로페이
     */
    private byte[] createQRAuthArrArr(ApprovalCode approvalCode) {

        byte[] code = approvalCode.getCode().getBytes();
        boolean isCancel;

        if(0 == 0){
            isCancel = false;
        }
        else
            isCancel = true;
        return new QRApproval(
                code,
                getDeviceId(),
                getQRWCC(),
                getQRTransactionType(),
                getQRData(),
                getIMonths(),
                getDealAmount(),
                getTax(),
                getSvcCharge(),
                getOrgDealDt(isCancel),
                getOrgApprovalNo(isCancel),
                getOrgUniqueNo(isCancel)
        ).create();
    }


    private byte[] createKakaopayArr(ApprovalCode approvalCode) {

        byte[] code = approvalCode.getCode().getBytes();
        boolean isCancel = approvalCode == ApprovalCode.KAKAO_CANCEL_INQUIRY;

        return new KakaoInqApproval(
                code,
                getDeviceId(),
                getKakaoPayWCC(),
                getKakaoPayData(),
                StringUtil.getLPadZero(12, money.trim()).getBytes(),
                StringUtil.getLPadZero(12, tax.trim()).getBytes(),
                StringUtil.getLPadZero(12, "0".trim()).getBytes(),
                getKakaoAuthDate(isCancel),
                getKakaoAuthNo(isCancel),
                getKakaoCancelReason(isCancel),
                getKakaoPayPosData()
        ).create();
    }

    private byte[] getKakaoCancelReason(boolean isCancel) {
        return isCancel ? "01".getBytes() : "  ".getBytes();
    }

    private byte[] getKakaoPayPosData() {
        //etApprovalIMonths;

        byte[] pPosData = new byte[12];
        Arrays.fill(pPosData,(byte)0x20);

        try {   // 할부
            System.arraycopy(iMonths.trim().getBytes(), 0, pPosData, 6, 2);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return pPosData;
    }


    private byte[] getKakaoPayWCC() {                   /// 바코드
        ZeroPayWCC wcc = ZeroPayWCC.getZeroPayWCC(ZeroPaywccs[0]);  // 일차원 바코드 or QR 바코드
        if (wcc != null) {
            return wcc.getCode().getBytes();
        }
        return StringUtil.getRPadSpace(1, "").getBytes();
    }

    private byte[] getKakaoPayData() {             // 바코드 데이터
        return StringUtil.getRPadSpace(24, "0".trim()).getBytes();
    }


    // 원거래 일자: ""
    private byte[] getKakaoAuthDate(boolean isCancel) {
        return StringUtil.getRPadSpace(8,
                isCancel ? "".trim() : ""
        ).getBytes();
    }

    /**
     * 원승인번호 (취소시)
     */
    private byte[] getKakaoAuthNo(boolean isCancel) {
        return StringUtil.getRPadSpace(10,
                isCancel ? "".trim() : ""
        ).getBytes();  // 원승인번호
    }


    private byte[] getQRWCC(){  // 바코드 0밖에 없음
        QRWcc qrWcc = QRWcc.getWcc(qrWCC[0]);

        if(qrWcc !=null){
            return qrWcc.getCode().getBytes();
        }
        return StringUtil.getRPadSpace(1, "").getBytes();

    }
    private byte[] getQRData(){   // 바코드
        return StringUtil.getRPadSpace(1024, "".trim()).getBytes();
    }
    private byte[] getQRTransactionType(){                                               // 카드번호 승인 or 취소
        QRTransactionType mgetQRTransactionType = QRTransactionType.getQRTransactionType(qrTransactionType[0]);

        if(mgetQRTransactionType !=null){
            return mgetQRTransactionType.getCode().getBytes();
        }
                                                    // 바코드
        return StringUtil.getRPadSpace(1, "").getBytes();
    }

    /**
     * 제로페이 입력 방식
     */
    private byte[] getZeroPayWCC() {                /// 바코드 종류
        ZeroPayWCC wcc = ZeroPayWCC.getZeroPayWCC(ZeroPaywccs[0]); // 일차원 바코드 or QR 바코드
        if (wcc != null) {
            return wcc.getCode().getBytes();
        }
        return StringUtil.getRPadSpace(1, "").getBytes();
    }

    /**
     * 제로페이 가맹점 임의 데이터
     */
    private byte[] getZeroPayRandomData() {
        try {                                   // 임의 데이터
            return StringUtil.getRPadSpace(50, "".trim()).getBytes("euc-kr");
        }
        catch(Exception e){                     // 임의 데이터
            return StringUtil.getRPadSpace(50, "".trim()).getBytes();
        }
    }

    /**
     * 제로페이 거래 방식
     */
    private byte[] getZeroPayGubun() {
        return "CPM".getBytes();
        /*
        ZeroPayGubun wcc = ZeroPayGubun.getZeroPayGubun(zeroPayGubuns[sp_approval_zero_kind.getSelectedItemPosition()]);
        if (wcc != null) {
            return wcc.getCode().getBytes();
        }
        return StringUtil.getRPadSpace(1, "").getBytes();

         */
    }

    /**
     * 제로페이 Track 2 Data
     */
    private byte[] getZeroPayData() {            /// 바코드 데이터
        return StringUtil.getRPadSpace(100, "".trim() + "=").getBytes();
    }

    /**
     * 사업자번호
     */
    private byte[] getBusinessNo() {            // 사업자번호
        return StringUtil.getRPadSpace(10, "8158100527".trim()).getBytes();
    }

    /**
     * 단위농협코드
     */
    private byte[] getBankCode() {        // 단위농협코드
        return StringUtil.getRPadSpace(6, "".trim()).getBytes();
    }


    /**
     * 발행일자
     */
    private byte[] getCheckPublishDt() {   // yyMMdd
        return StringUtil.getRPadSpace(6, "".trim()).getBytes();
    }

    /**
     * 수표금액
     */
    private byte[] getCheckAmount() {      // 수표금액
        return StringUtil.getLPadZero(9, "".trim()).getBytes();
    }

    /**
     * 수표번호
     */
    private byte[] getCheckNo() {          // 수표번호
        return StringUtil.getRPadSpace(16, "".trim()).getBytes();
    }

    /**
     * 비밀번호
     */
    private byte[] getPasswordInfo() {     // 비밀번호
        return StringUtil.getRPadSpace(18, "".trim()).getBytes();
    }

    /**
     * 포인트 종류
     */
    private byte[] getPontKindGubun() {
        PointKind pointKindGubun = PointKind.getPointKindGubun(pointKindGubuns[0]);  // OCB전자쿠폰, Oh 포인트, L-Point, 그 외
        if (pointKindGubun != null) {
            return pointKindGubun.getCode().getBytes();
        }
        return "  ".getBytes();
    }

    /**
     * 사용요청포인트
     */
    private byte[] getPointAmount() {        // 사용요청포인트
        return StringUtil.getLPadZero(9, "".trim()).getBytes();
    }

    /**
     * 적립구분
     */
    private byte[] getPontSaveGubun() {
        PointSaveGubun pointSaveGubun = PointSaveGubun.getPointSaveGubun(pointSaveGubuns[0]); // 적립/현금 (적립 Only, 현금영수증 제외), etc 종류들이 있다.
        if (pointSaveGubun != null) {
            return pointSaveGubun.getCode().getBytes();
        }
        return "00".getBytes();
    }

    /**
     * 은련 체크
     */
    private boolean isSelectUnionPay() {
        return additionalInfos[0].equals(AdditionalInfo.UNION_PAY.getMsg());  // 없음 or 해외은련카드
    }

    /**
     * 단말기 ID
     */
    private byte[] getDeviceId() {
        return StringUtil.getRPadSpace(10, tid).getBytes();
        //if (mainListener != null) { // mainListener: kr.co.softrain.mypaytest.sample.MainActivity@a1f9df7
        //    return StringUtil.getRPadSpace(10, cardreader).getBytes(); // [B@cf9c477
        //}
        //return StringUtil.getRPadSpace(10, "").getBytes();
    }

    /**
     * 카드번호
     */
    private byte[] getTrack2(boolean isKeyIn) {
        return StringUtil.getRPadSpace(100, "").getBytes();
//        if ("0" != null && isKeyIn) {
//            String cardNumber = "";
//            int length = cardNumber.length() + 1;
//            String track2 = length + cardNumber + "=";
//            return StringUtil.getRPadSpace(100, track2).getBytes();
//        }
//        return StringUtil.getRPadSpace(100, "").getBytes();
    }

    /**
     * 키인 체크
     */
    private boolean isSelectKeyIn() {
        return wccs[1].equals(Wcc.KEYIN.getMsg());  // Key-In or 리더기
    }

    /**
     * 카드 입력 방식
     */
    private byte[] getWCC() {
        Wcc wcc = Wcc.getWcc(wccs[1]); // Key-In or 리더기
        if (wcc != null) {
            return wcc.getCode().getBytes();
        }
        return StringUtil.getRPadSpace(1, "").getBytes();
    }

    /**
     * 할부
     */
    private byte[] getIMonths() {
        return StringUtil.getLPadZero(2, iMonths).getBytes(); // trim() 문자열의 앞뒤 공백을 제거하기 위해 사용
    }

    /**
     * 거래금액
     */
    private byte[] getDealAmount() {
        Log.e("거래금액 늘리기", " : " + money.trim());
        return StringUtil.getLPadZero(9, money.trim()).getBytes();
    }

    /**
     * 세금
     */
    private byte[] getTax() {
        return StringUtil.getLPadZero(9, tax.trim()).getBytes();
    }

    /**
     * 봉사료
     */
    private byte[] getSvcCharge() {
        return StringUtil.getLPadZero(9, svcCharge.trim()).getBytes();
    }

    /**
     * 원거래일자 (취소시)
     */
    private byte[] getOrgDealDt(boolean isCancel) {
        return StringUtil.getRPadSpace(6,
                isCancel ? drgDealDt.trim() : ""
        ).getBytes();
    }

    /**
     * 원승인번호 (취소시)
     */
    private byte[] getOrgApprovalNo(boolean isCancel) {
        return StringUtil.getRPadSpace(12,
                isCancel ? orgApprovalNo.trim() : ""
        ).getBytes();
    }

    /**
     * 원거래고유번호 (취소시)
     */
    private byte[] getOrgUniqueNo(boolean isCancel) {
        return StringUtil.getRPadSpace(12,
                isCancel ? orgUniqueNo.trim() : ""
        ).getBytes();
    }

    /**
     * 서명처리구분
     */
    private byte[] getSignature() {
        Signature signature = Signature.getSignature(signatures[3]); // 사용, 미사용, 재사용, 데몬판단처
        if (signature != null) {
            return signature.getCode().getBytes();
        }
        return Signature.NONE.getCode().getBytes();
    }

    /**
     * 거래구분자
     */
    private byte[] getDealGubun() {
        DealGubun dealGubun = DealGubun.getDealGubun(dealGubuns[0]); // 소비자소득공제 or 사업자지출증빙
        if (dealGubun != null) {
            return dealGubun.getCode().getBytes();
        }
        return "0".getBytes();
    }


}


















//                    String sentence = strResData;
//                    String[] words = sentence.split("\\s+"); // 띄어쓰기를 기준으로 분할
//                    List<String> wordsExcludingSpace = new ArrayList<>();
//                    for (String word : words) {
//                        if (!word.trim().isEmpty()) { // 띄어쓰기를 제외한 단어인 경우
//                            wordsExcludingSpace.add(word);
//
//                            Log.e("추출1", " : " +  sentence);
//                            Log.e("추출2", " : " +  words);
//                            Log.e("추출3", " : " +  word);
//
//                        }
//                    }
//                    Log.e("추출4", " : " +  wordsExcludingSpace);
//                    Log.e("추출5", " : " +  wordsExcludingSpace.size());