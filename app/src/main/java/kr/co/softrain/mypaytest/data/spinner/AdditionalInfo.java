package kr.co.softrain.mypaytest.data.spinner;

/**
 * 부가정보
 */
public enum AdditionalInfo {
    NONE("없음"),
//    ALIPAY("알리페이"),
    UNION_PAY("해외은련카드");

    private String msg;

    AdditionalInfo(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static AdditionalInfo getAdditionalInfo(String msg) {
        for (AdditionalInfo additionalInfo : AdditionalInfo.values()) {
            if (additionalInfo.msg.equals(msg)) {
                return additionalInfo;
            }
        }
        return null;
    }

    public static String[] getAdditionalInfoArr() {
        int length = AdditionalInfo.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = AdditionalInfo.values()[i].msg;
        }

        return strings;
    }
}
