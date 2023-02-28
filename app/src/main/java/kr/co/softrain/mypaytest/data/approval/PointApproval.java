package kr.co.softrain.mypaytest.data.approval;

import kr.co.softrain.mypaytest.utils.ByteUtil;
import kr.co.softrain.mypaytest.utils.StringUtil;

/**
 * 포인트 요청
 */
public class PointApproval extends BaseApproval {
    private byte[] wcc;
    private byte[] track2;
    private byte[] saveGubun;                     // 적립구분           dll pdf 29p
    private byte[] dealAmount;
    private byte[] useReqPoint;                   // 사용요청포인트        사용인 경우 사용 포인트, 그 외 '000000000'
    private byte[] pointKind = StringUtil.getRPadSpace(2, "").getBytes();                     // 포인트 종류          'GP':기가,'EP':OCB전자쿠폰,'OP':Oh포인트,'HP':하이굿 그 외 Space
    private final byte[] deviceGubun = StringUtil.getRPadSpace(2, "").getBytes();                   // 단말기 구분         BC Oh! 포인트인 경우 '01':CAT, '02':POS, '03':회원사, '04':WEB, '05':모바일, '06':기타
    //                  BC Oh! 포인트인 경우 Set, 그 외 Space
    private byte[] orgDealDt;
    private byte[] orgApprovalNo;
    private byte[] password;             // 비밀번호        	Key Index(2Byte; Number Type) + 암호화 PIN 데이터
    private final byte[] dBusinessNo = StringUtil.getRPadSpace(10, "").getBytes();      // 하위사업자번호   PG Off-Line 거래인 경우 Set, 그 외 Space
    private final byte[] additional1 = StringUtil.getRPadSpace(16, "").getBytes();      // 부가정보1
    private final byte[] additional2 = StringUtil.getRPadSpace(32, "").getBytes();      // 부가정보2
    private final byte[] additional3 = StringUtil.getRPadSpace(128, "").getBytes();     // 부가정보3

    public PointApproval(byte[] dealTypeCd,
                         byte[] terminalNo,
                         byte[] wcc,
                         byte[] track2,
                         byte[] saveGubun,
                         byte[] dealAmount,
                         byte[] useReqPoint,
                         byte[] pointKind,
                         byte[] orgDealDt,
                         byte[] orgApprovalNo,
                         byte[] passwordInfo
    ) {
        super(dealTypeCd, terminalNo);
        this.wcc = wcc;
        this.track2 = track2;
        this.saveGubun = saveGubun;
        this.dealAmount = dealAmount;
        this.useReqPoint = useReqPoint;
        this.pointKind = pointKind;
        this.orgDealDt = orgDealDt;
        this.orgApprovalNo = orgApprovalNo;
        this.password = passwordInfo;
    }

    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                super.create(),
                wcc,
                track2,
                saveGubun,
                dealAmount,
                useReqPoint,
                pointKind,
                deviceGubun,
                orgDealDt,
                orgApprovalNo,
                password,
                dBusinessNo,
                additional1,
                additional2,
                additional3,
                cr
        );
    }
}
