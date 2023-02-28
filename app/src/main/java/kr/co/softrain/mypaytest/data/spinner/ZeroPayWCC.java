package kr.co.softrain.mypaytest.data.spinner;

public enum ZeroPayWCC {

    CODE_01("B", "일차원 바코드"),
    CODE_02("Q", "QR 바코드");

    private String code;
    private String msg;

    ZeroPayWCC(String code, String msg) {
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

    public static ZeroPayWCC getZeroPayWCC(String msg) {
        for (ZeroPayWCC zeroPayWCC : ZeroPayWCC.values()) {
            if (zeroPayWCC.msg.equals(msg)) {
                return zeroPayWCC;
            }
        }
        return null;
    }

    public static String[] getZeroPayWCCArr() {
        int length = ZeroPayWCC.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = ZeroPayWCC.values()[i].msg;
        }

        return strings;
    }
}
