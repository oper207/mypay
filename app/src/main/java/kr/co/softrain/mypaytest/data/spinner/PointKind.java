package kr.co.softrain.mypaytest.data.spinner;

public enum PointKind {

    //GP("GP", "기가 포인트"),
    EP("EP", "OCB전자쿠폰"),
    OP("OP", "Oh 포인트"),
    LP("LP", "L-Point"),
    ETC("  ", "그 외");

    private String code;
    private String msg;

    PointKind(String code, String msg) {
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

    public static PointKind getPointKindGubun(String msg) {
        for (PointKind pointKindGubun : PointKind.values()) {
            if (pointKindGubun.msg.equals(msg)) {
                return pointKindGubun;
            }
        }
        return null;
    }

    public static String[] getPointKindGubunArr() {
        int length = PointKind.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = PointKind.values()[i].msg;
        }

        return strings;
    }
}
