package com.softrain.mypay.data.approval;


import com.softrain.mypay.utils.ByteUtil;
import com.softrain.mypay.utils.StringUtil;

/**
 * 현금 IC 조회
 */
public class CashIcInquiryApproval extends BaseApproval {

    private final byte[] dBusinessNo = StringUtil.getRPadSpace(10, "").getBytes();     // 하위사업자번호   PG Off-Line 거래인 경우 Set, 그 외 Space
    private final byte[] filler1 = StringUtil.getRPadSpace(64, "").getBytes();     // FILLER1
    private final byte[] filler2 = StringUtil.getRPadSpace(64, "").getBytes();     // FILLER2*/

    public CashIcInquiryApproval(byte[] dealTypeCd, byte[] terminalNo) {
        super(dealTypeCd, terminalNo);
    }

    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                super.create(),
                dBusinessNo,
                filler1,
                filler2,
                cr
        );
    }
}
