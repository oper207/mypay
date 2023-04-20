package com.softrain.mypay;

public class ConstDef {

    public static final String TAG = "dckim-mypay";

    public static final String INTENT_SEND_TAG = "intentSendMessage";
    public static final String INTENT_RECEIVE_TAG = "intentReceiveMessage";

    public static final String MYDISPLAY_IN_TAG = "extIO Mydisplay In";
    public static final String MYDISPLAY_OUT_TAG = "extIO Mydisplay Out";

    public static final String JETNET_REQUEST_TAG = "extIO Jetnet Request";
    public static final String JETNET_RESPONSE_TAG = "extIO Jetnet Response";
    public static final String JETNET_RESPONSE_NOT_NULL_TAG = "extIO Jetnet Response Not Null";

    public static final String ACTION_NAME_FROM_MYDISPLAY = "softrain.intent.action.pay";
    public static final String ACTION_NAME_TO_MYDISPLAY = "softrain.intent.action.sol";

    public static final String CMD_TYPE = "S_TriggerModuleFunc";
    public static final String CMD_FUNCNM_INTENT_RECEIVED = "$intentReceived";
}
