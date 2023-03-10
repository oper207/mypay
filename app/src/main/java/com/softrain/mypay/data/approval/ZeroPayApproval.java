package com.softrain.mypay.data.approval;

import com.softrain.mypay.utils.ByteUtil;
import com.softrain.mypay.utils.StringUtil;

/**
 * 제로페이
 */
public class ZeroPayApproval extends BaseApproval {
    private byte[] wcc;                                                                             // Wcc         		K:Key-In, 그 외 SPACE:보 안 리더기 처리
    private byte[] track2;                                                                          // TRACK-II			앱카드 Key-In 입력인 경우 카드번호 + '=' ('22' + '123456789012345678901='), 신용카드 Key-In 입력인 경우 카드번호 + ‘=’ + 유효기간 ('21' + '1234567890123456=1214')
    private byte[] kind;
    private byte[] dealAmount;                                                                      // 거래금액        	세금,봉사료 제외 금액
    private byte[] tax;                                                                             // 세금
    private byte[] svcCharge;                                                                       // 봉사료
    private byte[] orgDealDt;                                                                       // 원거래일자		취소인 경우:YYMMDD, 그 외 Space
    private byte[] orgApprovalNo;                                                                   // 원승인번호      	취소인 경우 Set
    private final byte[] taxExempt = StringUtil.getLPadZero(12, "").getBytes();       // 비과세 금액     	결제 금액과 무관.  비과세 금액 표시에 사용
    private final byte[] dBusinessNo = StringUtil.getRPadSpace(10, "").getBytes();      // 하위사업자번호   PG Off-Line 거래인 경우 Set, 그 외 Space
    private byte[] randomData;      // 부가정보1
    private final byte[] additional1 = StringUtil.getRPadSpace(64, "").getBytes();      // 부가정보2
    private final byte[] additional2 = StringUtil.getRPadSpace(64, "").getBytes();     // 부가정보3

    public ZeroPayApproval(
            byte[] dealTypeCd,
            byte[] terminalNo,
            byte[] wcc,
            byte[] track2,
            byte[] kind,
            byte[] dealAmount,
            byte[] tax,
            byte[] svcCharge,
            byte[] orgDealDt,
            byte[] orgApprovalNo,
            byte[] randomData
    ) {
        super(dealTypeCd, terminalNo);
        this.wcc = wcc;
        this.track2 = track2;
        this.kind = kind;
        this.dealAmount = dealAmount;
        this.tax = tax;
        this.svcCharge = svcCharge;
        this.orgDealDt = orgDealDt;
        this.orgApprovalNo = orgApprovalNo;
        this.randomData = randomData;
    }

    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                super.create(),
                wcc,
                track2,
                kind,
                dealAmount,
                tax,
                svcCharge,
                orgDealDt,
                orgApprovalNo,
                taxExempt,
                dBusinessNo,
                randomData,
                additional1,
                additional2,
                cr
        );
    }
}
