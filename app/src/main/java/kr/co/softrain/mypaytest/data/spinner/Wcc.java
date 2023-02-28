package kr.co.softrain.mypaytest.data.spinner;

public enum Wcc {
    KEYIN("K", "Key-In"),
    READER(" ", "리더기");

    private String code;
    private String msg;

    Wcc(String code, String msg) {
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

    public static Wcc getWcc(String msg) {
        for (Wcc wcc : Wcc.values()) {
            if (wcc.msg.equals(msg)) {
                return wcc;
            }
        }
        return null;
    }

    public static String[] getWccArr() {
        int length = Wcc.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = Wcc.values()[i].msg;
        }

        return strings;
    }

//    private final String[] sp_02_appCard = {"Q", "P", "A"};    // 앱카드(Q:QR/바코드 SCAN, P:NFC P2P, A:KeyIn)
//    private final String[] sp_02_alipay = {"B", "K"};            // 알리페이(B:바코드, K:KeyIn)
}
