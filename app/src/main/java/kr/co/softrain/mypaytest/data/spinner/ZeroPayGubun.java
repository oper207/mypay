package kr.co.softrain.mypaytest.data.spinner;

public enum ZeroPayGubun {

    CODE_01("CPM", "CPM"),
    CODE_02("MPM", "MPM");

    private String code;
    private String msg;

    ZeroPayGubun(String code, String msg) {
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

    public static ZeroPayGubun getZeroPayGubun(String msg) {
        for (ZeroPayGubun zeroPayGubun : ZeroPayGubun.values()) {
            if (zeroPayGubun.msg.equals(msg)) {
                return zeroPayGubun;
            }
        }
        return null;
    }

    public static String[] getZeroPayGubunArr() {
        int length = ZeroPayGubun.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = ZeroPayGubun.values()[i].msg;
        }

        return strings;
    }
}
