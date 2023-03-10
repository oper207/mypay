package com.softrain.mypay.data.spinner;

public enum QRWcc {
    BARCODE("U", "바코드");

    private String code;
    private String msg;

    QRWcc(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static QRWcc getWcc(String msg) {
        for (QRWcc wcc : QRWcc.values()) {
            if (wcc.msg.equals(msg)) {
                return wcc;
            }
        }
        return null;
    }

    public static String[] getWccArr() {
        int length = QRWcc.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = QRWcc.values()[i].msg;
        }

        return strings;
    }

//    private final String[] sp_02_appCard = {"Q", "P", "A"};    // 앱카드(Q:QR/바코드 SCAN, P:NFC P2P, A:KeyIn)
//    private final String[] sp_02_alipay = {"B", "K"};            // 알리페이(B:바코드, K:KeyIn)
}
