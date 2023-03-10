package com.softrain.mypay.data.approval;


import com.softrain.mypay.utils.ByteUtil;
import com.softrain.mypay.utils.StringUtil;

/**
 * 가맹점 다운
 */
public class StoreDownApproval extends BaseApproval {
    private final byte[] deviceType = StringUtil.getRPadSpace(1, "P").getBytes();          // 장치구분			P: POS, C: 단말기
    private final byte[] serialNoCheck = StringUtil.getRPadSpace(1, "!").getBytes();       // 일련번호체크		Y: 시리얼번호체크, !: 시리얼번호체크안함
    private byte[] businessNo;                                                                         // 사업자번호
    private final byte[] filler = StringUtil.getRPadSpace(16, "").getBytes();              // FILLER

    /**
     *
     * @param dealTypeCd 전문종류
     * @param terminalNo 단말기번호
     * @param businessNo 사업자번호
     */
    public StoreDownApproval(byte[] dealTypeCd, byte[] terminalNo, byte[] businessNo) {
        super(dealTypeCd, terminalNo);
        this.businessNo = businessNo;
    }

    public byte[] getDeviceType() {
        return deviceType;
    }

    public byte[] getSerialNoCheck() {
        return serialNoCheck;
    }

    public byte[] getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(byte[] businessNo) {
        this.businessNo = businessNo;
    }

    public byte[] getFiller() {
        return filler;
    }

    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                super.create(),
                deviceType,
                serialNoCheck,
                businessNo,
                filler,
                cr
        );
    }
}
