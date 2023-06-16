package com.softrain.mypay;

// 상수 값을 정의
public class ConstDef {
    public static final String TAG = "ymj-mypay";

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

    public static final String HTTP_TYPE_DATA = "HttpData";
    public static final String HTTP_TYPE_FILE = "HttpFile";

    public static final String UPDATE_URL_VERSION = "http://www.mydisplay.co.kr/sol/p/mypay/mypay_version.json";
    public static final String UPDATE_URL_APK = "http://www.mydisplay.co.kr/sol/p/mypay/mypay.apk";
    public static final String UPDATE_TIME1 = "04:15";
    public static final String UPDATE_TIME2 = "04:30";
    public static final String DOWNLOAD_DIR = "Download";
    public static final String UPLOAD_URL_LOG = "http://www.mydisplay.co.kr/sol/api/s3/saveFiles";

    public static final int START_ACTIVITY_TIME = 7000; /* 7 seconds */
    public static final int ALARM_TIME = 120000; /* 2 minutes */

    public static final int HTTP_GET_UPDATE_VERSION = 2000;
    public static final int HTTP_GET_UPDATE_APK = 2001;
    public static final int HTTP_POST_UPLOAD_LOG = 3000;

    /* Return Result Error Reason */
    public static final String RETURN_RESULT_VALUE_OK = "{{ OK }}";
    public static final String RETURN_RESULT_VALUE_NETWORK_DISCONNECTED = "{{ NetworkDisconnected }}";
    public static final String RETURN_RESULT_VALUE_SERVER_ERROR_RESPONSE = "{{ ServerErrorResponse }}";
    public static final String RETURN_RESULT_VALUE_SERVER_TIMEOUT = "{{ ServerTimeout }}";
}
