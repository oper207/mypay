package com.softrain.mypay.data.approval;

import com.softrain.mypay.utils.ByteUtil;
import com.softrain.mypay.utils.StringUtil;

public class KakaoInqApproval extends BaseApproval {
    private byte[] barcodetype;
    private byte[] barcode;
    private byte[] amount;
    private byte[] tax;
    private byte[] tip;
    private final byte[] cattype = {(byte)0x50};
    private final byte[] currency = "KRW".getBytes();
    private final byte[] storecode = StringUtil.getRPadSpace(12, "").getBytes();
    private final byte[] notax = "000000000000".getBytes();

    private byte[] authdate;
    private byte[] authno;
    private final byte[] canceltype = "0".getBytes();
    private byte[] cancelreason;
    private byte[] posoption;
    private final byte[] addinfo = StringUtil.getRPadSpace(64, "").getBytes();
    private final byte[] reserved = StringUtil.getRPadSpace(432, "").getBytes();


    public KakaoInqApproval(
            byte[] dealTypeCd,
            byte[] terminalNo,
            byte[] barcodetype,
            byte[] barcode,
            byte[] amount,
            byte[] tax,
            byte[] tip,
            byte[] authdate,
            byte[] authno,
            byte[] cancelreason,
            byte[] posoption){

        super(dealTypeCd, terminalNo);
        this.barcodetype = barcodetype;
        this.barcode = barcode;
        this.amount = amount;
        this.tax = tax;
        this.tip = tip;
        this.authdate = authdate;
        this.authno = authno;
        this.cancelreason = cancelreason;
        this.posoption = posoption;
    }
    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                super.create(),
                barcodetype,
                barcode,
                amount,
                tax,
                tip,
                cattype,
                currency,
                storecode,
                notax,
                authdate,
                authno,
                canceltype,
                cancelreason,
                posoption,
                addinfo,
                reserved,
                cr
        );
    }
}
