package com.softrain.mypay.data.approval;

import com.softrain.mypay.utils.ByteUtil;
import com.softrain.mypay.utils.StringUtil;

/**
 * 수표 조회
 */
public class CheckApproval extends BaseApproval {
    private byte[] checkNo;                 // 수표번호 수표번호(8)+은행코드(2)+지점코드(4)+권종코드(2)
    private byte[] checkAmount;             // 수표금액
    private byte[] orgDealDt;               // 발행일자 YYMMDD
    private byte[] nhCode;                  // 단위농협코드  수표번호 앞 두자리가 '11'인 경우 단위농협 코드 값을 넣는다.
    private final byte[] addtional = StringUtil.getRPadSpace(16, "").getBytes();               // 부가정보

    public CheckApproval(byte[] dealTypeCd,
                         byte[] terminalNo,
                         byte[] checkNo,
                         byte[] checkAmount,
                         byte[] orgDealDt,
                         byte[] nhCode) {
        super(dealTypeCd, terminalNo);
        this.checkNo = checkNo;
        this.checkAmount = checkAmount;
        this.orgDealDt = orgDealDt;
        this.nhCode = nhCode;
    }

    public byte[] getCheckNo() {
        return checkNo;
    }

    public void setCheckNo(byte[] checkNo) {
        this.checkNo = checkNo;
    }

    public byte[] getCheckAmount() {
        return checkAmount;
    }

    public void setCheckAmount(byte[] checkAmount) {
        this.checkAmount = checkAmount;
    }

    public byte[] getOrgDealDt() {
        return orgDealDt;
    }

    public void setOrgDealDt(byte[] orgDealDt) {
        this.orgDealDt = orgDealDt;
    }

    public byte[] getNhCode() {
        return nhCode;
    }

    public void setNhCode(byte[] nhCode) {
        this.nhCode = nhCode;
    }

    public byte[] getAddtional() {
        return addtional;
    }

    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                super.create(),
                checkNo,
                checkAmount,
                orgDealDt,
                isNH() ? nhCode : StringUtil.getRPadSpace(6, "").getBytes(),
                addtional,
                cr
        );
    }

    /**
     * 수표번호 앞 두자리가 '11'인 경우 단위농협 코드 값을 넣는다.
     */
    private boolean isNH() {
        if (checkNo != null) {
            return checkNo[0] == 0x31 && checkNo[1] == 0x31;
        }
        return false;
    }
}
