package kr.co.softrain.mypaytest.data.spinner;

public enum QRTransactionType {

    CODE_01("A", "승인"),
    CODE_02("C", "취소");

    private String code;
    private String msg;

    QRTransactionType(String code, String msg) {
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

    public static QRTransactionType getQRTransactionType(String msg) {
        for (QRTransactionType mQRTransactionType : QRTransactionType.values()) {
            if (mQRTransactionType.msg.equals(msg)) {
                return mQRTransactionType;
            }
        }
        return null;
    }

    public static String[] getQRTransactionTypeArr() {
        int length = QRTransactionType.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = QRTransactionType.values()[i].msg;
        }

        return strings;
    }
}
