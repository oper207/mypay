package com.softrain.mypay.data.ability;

import com.softrain.mypay.utils.ByteUtil;
import com.softrain.mypay.utils.StringUtil;

import java.io.UnsupportedEncodingException;

/**
 * 서명
 */
public class SignAbility extends BaseAbility {

    private byte[] amount;

    private final String strDisplayMsg1 = StringUtil.getRPadSpace(16, "서명하세요1");
    private final String strDisplayMsg2 = StringUtil.getRPadSpace(16, "서명하세요2");
    private final String strDisplayMsg3 = StringUtil.getRPadSpace(16, "서명하세요3");
    private final String strDisplayMsg4 = StringUtil.getRPadSpace(16, "서명하세요4");

    private byte[] displayMsg1;
    private byte[] displayMsg2;
    private byte[] displayMsg3;
    private byte[] displayMsg4;

    public SignAbility(byte[] requestCode, byte[] amount) {
        super(requestCode);
        this.amount = amount;

        try {
            displayMsg1 = strDisplayMsg1.getBytes("EUC-KR");
            displayMsg2 = strDisplayMsg2.getBytes("EUC-KR");
            displayMsg3 = strDisplayMsg3.getBytes("EUC-KR");
            displayMsg4 = strDisplayMsg4.getBytes("EUC-KR");
        } catch (UnsupportedEncodingException e) {
            displayMsg1 = strDisplayMsg1.getBytes();
            displayMsg2 = strDisplayMsg2.getBytes();
            displayMsg3 = strDisplayMsg3.getBytes();
            displayMsg4 = strDisplayMsg4.getBytes();
        }
    }

    public byte[] getAmount() {
        return amount;
    }

    public void setAmount(byte[] amount) {
        this.amount = amount;
    }

    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                requestCode,
                amount,
                displayMsg1,
                displayMsg2,
                displayMsg3,
                displayMsg4,
                cr
        );
    }
}
