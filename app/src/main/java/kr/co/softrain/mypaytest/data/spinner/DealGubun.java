package kr.co.softrain.mypaytest.data.spinner;

public enum DealGubun {
    CONSUMER("0", "소비자소득공제"),
    BUSINESS("1", "사업자지출증빙");

    private String code;
    private String msg;

    DealGubun(String code, String msg) {
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

    public static DealGubun getDealGubun(String msg) {
        for (DealGubun dealGubun : DealGubun.values()) {
            if (dealGubun.msg.equals(msg)) {
                return dealGubun;
            }
        }
        return null;
    }

    public static String[] getDealGubunArr() {
        int length = DealGubun.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = DealGubun.values()[i].msg;
        }

        return strings;
    }
}
