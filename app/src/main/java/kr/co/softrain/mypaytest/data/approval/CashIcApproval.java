package kr.co.softrain.mypaytest.data.approval;

import kr.co.softrain.mypaytest.utils.ByteUtil;
import kr.co.softrain.mypaytest.utils.StringUtil;

/**
 * 현금 IC
 */
public class CashIcApproval extends BaseApproval {

    private byte[] dealAmount;                                                                               // 거래금액        	세금,봉사료 제외 금액
    private byte[] tax;                                                                               // 세금
    private byte[] svcCharge;                                                                               // 봉사료
    private byte[] dealGubun;                                                                                   // 거래구분자		'0':소비자 소득공제, '1':사업자지출증빙
    private byte[] orgDealDt;                                                                       // 원거래일자		취소인 경우:YYMMDD, 그 외 Space
    private byte[] orgApprovalNo;                                                                   // 원승인번호      	취소인 경우 Set
    private final byte[] taxExempt = StringUtil.getLPadZero(9, "").getBytes();                // 비과세 금액     	결제 금액과 무관.  비과세 금액 표시에 사용
    private final byte[] dBusinessNo = StringUtil.getRPadSpace(10, "").getBytes();                    // 하위사업자번호   PG Off-Line 거래인 경우 Set, 그 외 Space
    private final byte[] strReserved1 = StringUtil.getRPadSpace(64, "").getBytes();                    // Reserved    		Space Set
    private final byte[] strReserved2 = StringUtil.getRPadSpace(64, "").getBytes();                    // Reserved    		Space Set


    public CashIcApproval(byte[] dealTypeCd,
                          byte[] terminalNo,
                          byte[] dealAmount,
                          byte[] tax,
                          byte[] svcCharge,
                          byte[] dealGubun,
                          byte[] orgDealDt,
                          byte[] orgApprovalNo) {
        super(dealTypeCd, terminalNo);
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
                dealAmount,
                tax,
                svcCharge,
                dealGubun,
                orgDealDt,
                orgApprovalNo,
                taxExempt,
                dBusinessNo,
                strReserved1,
                strReserved2,
                cr
        );
    }
}
