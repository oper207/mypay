package com.softrain.mypay.data.approval;


import com.softrain.mypay.utils.ByteUtil;
import com.softrain.mypay.utils.StringUtil;

/**
 * 현금영수증
 */
public class CashApproval extends BaseApproval {
    private byte[] wcc;                                                                             // Wcc         		K:Key-In, 그 외 SPACE:보 안 리더기 처리
    private byte[] track2;                                                                          // TRACK-II			앱카드 Key-In 입력인 경우 카드번호 + '=' ('22' + '123456789012345678901='), 신용카드 Key-In 입력인 경우 카드번호 + ‘=’ + 유효기간 ('21' + '1234567890123456=1214')
    private byte[] dealAmount;                                                                      // 거래금액        	세금,봉사료 제외 금액
    private byte[] tax;                                                                             // 세금
    private byte[] svcCharge;                                                                       // 봉사료
    private byte[] dealGubun;                                                                       // 거래구분자		'0':소비자 소득공제, '1':사업자지출증빙
    private byte[] orgDealDt;                                                                       // 원거래일자		취소인 경우:YYMMDD, 그 외 Space
    private byte[] orgApprovalNo;                                                                   // 원승인번호      	취소인 경우 Set
    private final byte[] cancelReason = StringUtil.getRPadSpace(1, "1").getBytes();     // 취소사유코드  	취소인 경우 1:거래취소, 2:오류발급취소, 3:기타, 그 외 Space
    private final byte[] taxExempt = StringUtil.getLPadZero(9, "").getBytes();       // 비과세 금액     	결제 금액과 무관.  비과세 금액 표시에 사용
    private final byte[] dBusinessNo = StringUtil.getRPadSpace(10, "").getBytes();      // 하위사업자번호   PG Off-Line 거래인 경우 Set, 그 외 Space
    private final byte[] additional1 = StringUtil.getRPadSpace(16, "").getBytes();      // 부가정보1
    private final byte[] additional2 = StringUtil.getRPadSpace(32, "").getBytes();      // 부가정보2
    private final byte[] additional3 = StringUtil.getRPadSpace(128, "").getBytes();     // 부가정보3

    public CashApproval(
            byte[] dealTypeCd,
            byte[] terminalNo,
            byte[] wcc,
            byte[] track2,
            byte[] dealAmount,
            byte[] tax,
            byte[] svcCharge,
            byte[] dealGubun,
            byte[] orgDealDt,
            byte[] orgApprovalNo
    ) {
        super(dealTypeCd, terminalNo);
        this.wcc = wcc;
        this.track2 = track2;
        this.dealAmount = dealAmount;
        this.tax = tax;
        this.svcCharge = svcCharge;
        this.dealGubun = dealGubun;
        this.orgDealDt = orgDealDt;
        this.orgApprovalNo = orgApprovalNo;
    }

    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                super.create(),
                wcc,
                track2,
                dealAmount,
                tax,
                svcCharge,
                dealGubun,
                orgDealDt,
                orgApprovalNo,
                cancelReason,
                taxExempt,
                dBusinessNo,
                additional1,
                additional2,
                additional3,
                cr
        );
    }
}
