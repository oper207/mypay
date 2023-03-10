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
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.softrain.mypay.common.HexCode;
import com.softrain.mypay.data.ability.BaseAbility;
import com.softrain.mypay.data.ability.PlainNumAbility;
import com.softrain.mypay.data.ability.SignAbility;
import com.softrain.mypay.data.approval.CashApproval;
import com.softrain.mypay.data.approval.CashIcApproval;
import com.softrain.mypay.data.approval.CashIcInquiryApproval;
import com.softrain.mypay.data.approval.CheckApproval;
import com.softrain.mypay.data.approval.CreditApproval;
import com.softrain.mypay.data.approval.KakaoInqApproval;
import com.softrain.mypay.data.approval.MembershipApproval;
import com.softrain.mypay.data.approval.PointApproval;
import com.softrain.mypay.data.approval.QRApproval;
import com.softrain.mypay.data.approval.StoreDownApproval;
import com.softrain.mypay.data.approval.ZeroPayApproval;
import com.softrain.mypay.data.spinner.AdditionalInfo;
import com.softrain.mypay.data.spinner.DealGubun;
import com.softrain.mypay.data.spinner.PointKind;
import com.softrain.mypay.data.spinner.PointSaveGubun;
import com.softrain.mypay.data.spinner.QRTransactionType;
import com.softrain.mypay.data.spinner.QRWcc;
import com.softrain.mypay.data.spinner.Signature;
import com.softrain.mypay.data.spinner.TradeGubun;
import com.softrain.mypay.data.spinner.Wcc;
import com.softrain.mypay.data.spinner.ZeroPayGubun;
import com.softrain.mypay.data.spinner.ZeroPayWCC;
import com.softrain.mypay.data.spinner.code.ApprovalCode;
import com.softrain.mypay.data.spinner.code.RequestCode;
import com.softrain.mypay.utils.StringUtil;
import net.jt.pos.sdk.JTNetPosManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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
    // ???, ??????, TID
    private String money, tax, tid, rcode;
    // ????????? ?????? ??????(??????, ?????????)
    private String iMonths, svcCharge = "0";
    // ????????? ?????? ??????(??????????????? (?????????), ??????????????? (?????????), ????????????????????? (?????????))
    private String drgDealDt, orgApprovalNo, orgUniqueNo = "";
//    // todo ????????? ?????????
//    String a = "0097";

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
        Log.e(ConstDef.TAG," MyService : mydisplay  ??? ?????? ???1 " + mCurrType);
        Log.e(ConstDef.TAG," MyService : mydisplay  ??? ?????? ???2 " + mCurrFuncNm);
        Log.e(ConstDef.TAG," MyService : mydisplay  ??? ?????? ???3 " + mCurrData);
        Log.e(ConstDef.TAG," MyService : mydisplay  ??? ?????? ???4 " + rcode);
        // MydisPlay?????? ????????? ???
        TextLog.LogResponse(" [Mydisplay ????????? ???] " +ConstDef.SERVICETAG + " ????????????: " + getNowTime() + " ????????? ??????: " + mCurrType + " : " + mCurrFuncNm + " : " + mCurrData);
        // ????????? ????????? ?????? ?????? ?????????
        requestTaskDaemon(requestCode);
    }

    // key change method
    private void handleKeyChange(){
        requestCode = ApprovalCode.getApproval("KC");
        // ????????? ????????? ?????? ?????? ?????????
        requestTaskDaemon(requestCode);
    }

    public MyPayService() {

        // mydisplay?????? ???????????? ???
        mMdCommand = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleRequestMain(intent);
            }
        };

        // ?????? ????????? ????????? ?????? ???
        requestCallback = new JTNetPosManager.RequestCallback() {
            @Override
            public void onResponse(Message msg) {
                byte[] response = msg.getData().getByteArray("RESPONSE_MSG");
                //Log.e("onResponse1", "[??????2] : " + response.length + "||" + response[response.length - 1]);
                if (response != null) {
                    String strResData = StringUtil.byteArrayToString(response);
                    // a+" "+
                    String text = strResData+" "+money+" "+tax;
                    String[] words = text.split("\\s+"); // ??????????????? ????????? ???????????? ????????? ??????
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

                    if(words[0].contains("0097")) {  // startsWith?
                        handleKeyChange();
                        // todo ????????? ?????????
//                        a = "keychange";
                    } else {
                        // {response0: "asdasdfqee", B: "adgdfgsgfgdfgfdgdfg2545545"}
//                        String sData = "{\"response0\":\"asdasdfqee\", \"B\":\"adgdfgsgfgdfgfdgdfg2545545\"}";
                        Log.e(ConstDef.TAG," MyService ????????? ?????? ???"+  jsonObject.toString());
                        Intent intent = new Intent(ConstDef.ACTION_NAME_TO_MYDISPLAY);
                        intent.putExtra("type", ConstDef.CMD_TYPE);
                        intent.putExtra("funcNm", ConstDef.CMD_FUNCNM_INTENT_RECEIVED);
                        intent.putExtra("data", jsonObject.toString());
                        sendBroadcast(intent);
                        // MydisPlay?????? ????????? ???
                        TextLog.LogResponse(" [Mydisplay??? ????????? ???] " +ConstDef.SERVICETAG + " ????????????: " + getNowTime() + " ?????? ??????: " + ConstDef.CMD_TYPE + " : " + ConstDef.CMD_FUNCNM_INTENT_RECEIVED + " : " + strResData);
                    }

                }
            }
        };
    }

    @Override
    public synchronized void onCreate() {
        super.onCreate();

        //?????? ?????? ?????? ?????? ??? ????????? ?????? ??????
        JTNetPosManager.init(getApplicationContext());
        // ?????? ?????????
        TextLog.init(this);
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        showNotification();
        // intent?????? ????????????
        IntentFilter filterMdCommand = new IntentFilter();
        filterMdCommand.addAction(ConstDef.ACTION_NAME_FROM_MYDISPLAY);
        registerReceiver(mMdCommand, filterMdCommand);

        return START_STICKY;
    }

    @Override
    public synchronized void onDestroy() {
        // ?????? ??????
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
            NotificationChannel channel = new NotificationChannel(channel_id, "?????? ??????", NotificationManager.IMPORTANCE_DEFAULT);

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

    // ????????? ????????? ?????? ?????? ?????????
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
        // ?????? ?????? ?????? ?????? ??????
        // requestArr????????? ????????? ???????????? ????????? ????????? ???????????? ????????? ????????????
        // requestCallback ????????? onResponse()???????????? ?????? ???????????? ?????? ????????????.
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
     * ?????? ??????
     */
    private byte[] createCreditArr(ApprovalCode approvalCode) {

        byte[] code = approvalCode.getCode().getBytes();
        boolean isCancel = approvalCode == ApprovalCode.CREDIT_CANCEL;

        // ???????????? ????????????
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
     * ???????????????
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
     * ????????????
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
     * ?????????
     */
    private byte[] createMembershipArr(ApprovalCode approvalCode) {

        // todo ?????? ?????? ?????? ?????? ??????
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
     * ?????? IC
     */
    private byte[] createCashIcArr(ApprovalCode approvalCode) {

        // todo ?????? ?????? ?????? ?????? ??????
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
     * ?????? IC ??????
     */
    private byte[] createCashIcInquiryArr(ApprovalCode approvalCode) {
        return new CashIcInquiryApproval(
                approvalCode.getCode().getBytes(),
                getDeviceId()
        ).create();
    }

    /**
     * ????????????
     */
    private byte[] getTradeGubun() {
        TradeGubun tradeGubun = TradeGubun.getDealGubun(tradeGubuns[0]); // ???????????? or ???????????????
        if (tradeGubun != null) {
            return tradeGubun.getCode().getBytes();
        }
        return "00".getBytes();
    }


    /**
     * ?????????
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
     * ????????? ??????
     */
    private byte[] createStoreDownArr(ApprovalCode approvalCode) {
        return new StoreDownApproval(
                approvalCode.getCode().getBytes(),
                getDeviceId(),
                getBusinessNo()
        ).create();
    }

    /**
     * ????????????
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
     * ????????????
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

        try {   // ??????
            System.arraycopy(iMonths.trim().getBytes(), 0, pPosData, 6, 2);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return pPosData;
    }


    private byte[] getKakaoPayWCC() {                   /// ?????????
        ZeroPayWCC wcc = ZeroPayWCC.getZeroPayWCC(ZeroPaywccs[0]);  // ????????? ????????? or QR ?????????
        if (wcc != null) {
            return wcc.getCode().getBytes();
        }
        return StringUtil.getRPadSpace(1, "").getBytes();
    }

    private byte[] getKakaoPayData() {             // ????????? ?????????
        return StringUtil.getRPadSpace(24, "0".trim()).getBytes();
    }


    // ????????? ??????: ""
    private byte[] getKakaoAuthDate(boolean isCancel) {
        return StringUtil.getRPadSpace(8,
                isCancel ? "".trim() : ""
        ).getBytes();
    }

    /**
     * ??????????????? (?????????)
     */
    private byte[] getKakaoAuthNo(boolean isCancel) {
        return StringUtil.getRPadSpace(10,
                isCancel ? "".trim() : ""
        ).getBytes();  // ???????????????
    }


    private byte[] getQRWCC(){  // ????????? 0?????? ??????
        QRWcc qrWcc = QRWcc.getWcc(qrWCC[0]);

        if(qrWcc !=null){
            return qrWcc.getCode().getBytes();
        }
        return StringUtil.getRPadSpace(1, "").getBytes();

    }
    private byte[] getQRData(){   // ?????????
        return StringUtil.getRPadSpace(1024, "".trim()).getBytes();
    }
    private byte[] getQRTransactionType(){                                               // ???????????? ?????? or ??????
        QRTransactionType mgetQRTransactionType = QRTransactionType.getQRTransactionType(qrTransactionType[0]);

        if(mgetQRTransactionType !=null){
            return mgetQRTransactionType.getCode().getBytes();
        }
                                                    // ?????????
        return StringUtil.getRPadSpace(1, "").getBytes();
    }

    /**
     * ???????????? ?????? ??????
     */
    private byte[] getZeroPayWCC() {                /// ????????? ??????
        ZeroPayWCC wcc = ZeroPayWCC.getZeroPayWCC(ZeroPaywccs[0]); // ????????? ????????? or QR ?????????
        if (wcc != null) {
            return wcc.getCode().getBytes();
        }
        return StringUtil.getRPadSpace(1, "").getBytes();
    }

    /**
     * ???????????? ????????? ?????? ?????????
     */
    private byte[] getZeroPayRandomData() {
        try {                                   // ?????? ?????????
            return StringUtil.getRPadSpace(50, "".trim()).getBytes("euc-kr");
        }
        catch(Exception e){                     // ?????? ?????????
            return StringUtil.getRPadSpace(50, "".trim()).getBytes();
        }
    }

    /**
     * ???????????? ?????? ??????
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
     * ???????????? Track 2 Data
     */
    private byte[] getZeroPayData() {            /// ????????? ?????????
        return StringUtil.getRPadSpace(100, "".trim() + "=").getBytes();
    }

    /**
     * ???????????????
     */
    private byte[] getBusinessNo() {            // ???????????????
        return StringUtil.getRPadSpace(10, "8158100527".trim()).getBytes();
    }

    /**
     * ??????????????????
     */
    private byte[] getBankCode() {        // ??????????????????
        return StringUtil.getRPadSpace(6, "".trim()).getBytes();
    }


    /**
     * ????????????
     */
    private byte[] getCheckPublishDt() {   // yyMMdd
        return StringUtil.getRPadSpace(6, "".trim()).getBytes();
    }

    /**
     * ????????????
     */
    private byte[] getCheckAmount() {      // ????????????
        return StringUtil.getLPadZero(9, "".trim()).getBytes();
    }

    /**
     * ????????????
     */
    private byte[] getCheckNo() {          // ????????????
        return StringUtil.getRPadSpace(16, "".trim()).getBytes();
    }

    /**
     * ????????????
     */
    private byte[] getPasswordInfo() {     // ????????????
        return StringUtil.getRPadSpace(18, "".trim()).getBytes();
    }

    /**
     * ????????? ??????
     */
    private byte[] getPontKindGubun() {
        PointKind pointKindGubun = PointKind.getPointKindGubun(pointKindGubuns[0]);  // OCB????????????, Oh ?????????, L-Point, ??? ???
        if (pointKindGubun != null) {
            return pointKindGubun.getCode().getBytes();
        }
        return "  ".getBytes();
    }

    /**
     * ?????????????????????
     */
    private byte[] getPointAmount() {        // ?????????????????????
        return StringUtil.getLPadZero(9, "".trim()).getBytes();
    }

    /**
     * ????????????
     */
    private byte[] getPontSaveGubun() {
        PointSaveGubun pointSaveGubun = PointSaveGubun.getPointSaveGubun(pointSaveGubuns[0]); // ??????/?????? (?????? Only, ??????????????? ??????), etc ???????????? ??????.
        if (pointSaveGubun != null) {
            return pointSaveGubun.getCode().getBytes();
        }
        return "00".getBytes();
    }

    /**
     * ?????? ??????
     */
    private boolean isSelectUnionPay() {
        return additionalInfos[0].equals(AdditionalInfo.UNION_PAY.getMsg());  // ?????? or ??????????????????
    }

    /**
     * ????????? ID
     */
    private byte[] getDeviceId() {
        return StringUtil.getRPadSpace(10, tid).getBytes();
        //if (mainListener != null) { // mainListener: kr.co.softrain.mypaytest.sample.MainActivity@a1f9df7
        //    return StringUtil.getRPadSpace(10, cardreader).getBytes(); // [B@cf9c477
        //}
        //return StringUtil.getRPadSpace(10, "").getBytes();
    }

    /**
     * ????????????
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
     * ?????? ??????
     */
    private boolean isSelectKeyIn() {
        return wccs[1].equals(Wcc.KEYIN.getMsg());  // Key-In or ?????????
    }

    /**
     * ?????? ?????? ??????
     */
    private byte[] getWCC() {
        Wcc wcc = Wcc.getWcc(wccs[1]); // Key-In or ?????????
        if (wcc != null) {
            return wcc.getCode().getBytes();
        }
        return StringUtil.getRPadSpace(1, "").getBytes();
    }

    /**
     * ??????
     */
    private byte[] getIMonths() {
        return StringUtil.getLPadZero(2, iMonths).getBytes(); // trim() ???????????? ?????? ????????? ???????????? ?????? ??????
    }

    /**
     * ????????????
     */
    private byte[] getDealAmount() {
        Log.e("???????????? ?????????", " : " + money.trim());
        return StringUtil.getLPadZero(9, money.trim()).getBytes();
    }

    /**
     * ??????
     */
    private byte[] getTax() {
        return StringUtil.getLPadZero(9, tax.trim()).getBytes();
    }

    /**
     * ?????????
     */
    private byte[] getSvcCharge() {
        return StringUtil.getLPadZero(9, svcCharge.trim()).getBytes();
    }

    /**
     * ??????????????? (?????????)
     */
    private byte[] getOrgDealDt(boolean isCancel) {
        return StringUtil.getRPadSpace(6,
                isCancel ? drgDealDt.trim() : ""
        ).getBytes();
    }

    /**
     * ??????????????? (?????????)
     */
    private byte[] getOrgApprovalNo(boolean isCancel) {
        return StringUtil.getRPadSpace(12,
                isCancel ? orgApprovalNo.trim() : ""
        ).getBytes();
    }

    /**
     * ????????????????????? (?????????)
     */
    private byte[] getOrgUniqueNo(boolean isCancel) {
        return StringUtil.getRPadSpace(12,
                isCancel ? orgUniqueNo.trim() : ""
        ).getBytes();
    }

    /**
     * ??????????????????
     */
    private byte[] getSignature() {
        Signature signature = Signature.getSignature(signatures[3]); // ??????, ?????????, ?????????, ???????????????
        if (signature != null) {
            return signature.getCode().getBytes();
        }
        return Signature.NONE.getCode().getBytes();
    }

    /**
     * ???????????????
     */
    private byte[] getDealGubun() {
        DealGubun dealGubun = DealGubun.getDealGubun(dealGubuns[0]); // ????????????????????? or ?????????????????????
        if (dealGubun != null) {
            return dealGubun.getCode().getBytes();
        }
        return "0".getBytes();
    }

    static String getNowTime() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formatedNow = formatter.format(now);
        return formatedNow;
    }

}













