package kr.co.softrain.mypaytest.data.spinner;

public enum PointSaveGubun {

    CODE_01("01", "적립/현금 (적립 Only, 현금영수증 제외)"),
    CODE_02("02", "적립/신용카드거래"),
    CODE_03("03", "적립/직불카드거래"),
    CODE_04("04", "적립/자사카드거래"),
    CODE_05("05", "적립/현금영수증 (동시 처리)"),
    CODE_09("09", "적립/현금 (현금영수증 Only, 적립은 제외)"),
    CODE_21("21", "사용/대금 지불수단으로 사용"),
    CODE_22("22", "사용/사은품 지급수단으로 사용"),
    CODE_23("23", "사용/환불(현금으로 환불)"),
    CODE_41("41", "적립/현금 취소 (적립 취소 Only, 현금영수증 취소는 제외)"),
    CODE_42("42", "사용/취소"),
    CODE_43("43", "적립/현금영수증 취소 (동시 취소 처리, 현금이 아닌 경우 모두 ‘43’Fix)"),
    CODE_44("44", "적립/현금영수증 취소 (현금영수증 취소Only, 적립취소는 제외)"),
    CODE_60("60", "포인트 조회, 삼성 PayBack 사용/취소/조회"),
    CODE_00("00", "일반 포인트 조회");

    private String code;
    private String msg;

    PointSaveGubun(String code, String msg) {
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

    public static PointSaveGubun getPointSaveGubun(String msg) {
        for (PointSaveGubun pointSaveGubun : PointSaveGubun.values()) {
            if (pointSaveGubun.msg.equals(msg)) {
                return pointSaveGubun;
            }
        }
        return null;
    }

    public static String[] getPointSveGubunArr() {
        int length = PointSaveGubun.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = PointSaveGubun.values()[i].msg;
        }

        return strings;
    }
}
