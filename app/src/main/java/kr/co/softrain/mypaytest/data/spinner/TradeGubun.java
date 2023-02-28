package kr.co.softrain.mypaytest.data.spinner;

public enum TradeGubun {

    NORMAL("00", "일반거래"),
    SIMPLE("01", "간소화거래");

    private String code;
    private String msg;

    TradeGubun(String code, String msg) {
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

    public static TradeGubun getDealGubun(String msg) {
        for (TradeGubun tradeGubun : TradeGubun.values()) {
            if (tradeGubun.msg.equals(msg)) {
                return tradeGubun;
            }
        }
        return null;
    }

    public static String[] getDealGubunArr() {
        int length = TradeGubun.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = TradeGubun.values()[i].msg;
        }

        return strings;
    }
}
