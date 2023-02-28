package kr.co.softrain.mypaytest.data.approval;

import kr.co.softrain.mypaytest.utils.ByteUtil;
import kr.co.softrain.mypaytest.utils.StringUtil;

/**
 * 멤버쉽
 */
public class MembershipApproval extends BaseApproval {

    private byte[] wcc;                                                                                  // Wcc         		K:Key-In, 그 외 SPACE:보 안 리더기 처리
    private byte[] track2;                                                                                                    // TRACK-II
    private byte[] dealAmount;                                                                              // 거래금액        	세금,봉사료 제외 금액
    private final byte[] cardPassword = StringUtil.getRPadSpace(18, "").getBytes();                    // 비밀번호        	Key Index(2Byte; Number Type) + 암호화 PIN 데이터
    private final byte[] dBusinessNo = StringUtil.getRPadSpace(10, "").getBytes();                    // 하위사업자번호   PG Off-Line 거래인 경우 Set, 그 외 Space
    private final byte[] additional1 = StringUtil.getRPadSpace(16, "").getBytes();                    // 부가정보1
    private final byte[] additional2 = StringUtil.getRPadSpace(32, "").getBytes();                    // 부가정보2
    private final byte[] additional3 = StringUtil.getRPadSpace(128, "").getBytes();                // 부가정보3
    private byte[] orgDealDt;                                                                               // 원거래일자		취소인 경우:YYMMDD, 그 외 Space
    private byte[] orgApprovalNo;                                                                           // 원승인번호      	취소인 경우 Set*/

    public MembershipApproval(byte[] dealTypeCd,
                              byte[] terminalNo,
                              byte[] wcc,
                              byte[] track2,
                              byte[] dealAmount,
                              byte[] orgDealDt,
                              byte[] orgApprovalNo) {
        super(dealTypeCd, terminalNo);
        this.wcc = wcc;
        this.track2 = track2;
        this.dealAmount = dealAmount;
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
                orgDealDt,
                orgApprovalNo,
                cardPassword,
                dBusinessNo,
                additional1,
                additional2,
                additional3,
                cr
        );
    }
}
