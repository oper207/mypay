package com.softrain.mypay;

// 상수 값을 정의
public class ConstDef {

    public static final String TAG = "dckim-mypay";

    public static final String INTENT_SEND_TAG = "intentSendMessage";
    public static final String INTENT_RECEIVE_TAG = "intentReceiveMessage";

    public static final String MYDISPLAY_TO_MYPAY_TAG = "extIO Mydisplay To Mypay";
    public static final String MYPAYT_TO_MYDISPLAY_TAG = "extIO Mypay To Mydisplay";

    public static final String MYDISPLAY_TO_MYPAY_GENERAL_PAYMENT_TAG = "extIO Mydisplay To Mypay General Payment";
    public static final String MYDISPLAY_TO_MYPAY_CANCEL_PAYMENT_TAG = "extIO Mydisplay To Mypay Cancel Payment";
    public static final String MYDISPLAY_TO_MYPAY_PRINT_TAG = "extIO Mydisplay To Mypay Print";

    public static final String KIS_PAYMENT_REQUEST_TAG = "extIO Kis Payment Request";
    public static final String KIS_PAYMENT_RESPONSE_TAG = "extIO Kis Payment Response";

    public static final String ACTION_NAME_FROM_MYDISPLAY = "softrain.intent.action.pay";
    public static final String ACTION_NAME_TO_MYDISPLAY = "softrain.intent.action.sol";

    public static final String CMD_TYPE = "S_TriggerModuleFunc";
    public static final String CMD_FUNCNM_INTENT_RECEIVED = "$intentReceived";
}
