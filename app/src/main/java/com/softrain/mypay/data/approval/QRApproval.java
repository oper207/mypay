package com.softrain.mypay.data.approval;

import com.softrain.mypay.utils.ByteUtil;
import com.softrain.mypay.utils.StringUtil;

/**
 * 신용
 */
public class QRApproval extends BaseApproval {
    private byte[] wcc;                                                                                     // Wcc         		K:Key-In, 앱카드(Q:QR/바코드 SCAN, P:NFC P2P, A: KeyIn),  알리페이(B바코드, K:KeyIn)그 외 SPACE:보 안 리더기 처리
    private byte[] TransactionType;
    private byte[] QRData;                                                                                  // TRACK-II			앱카드 Key-In 입력인 경우 카드번호 + '=' ('22' + '123456789012345678901='), 신용카드 Key-In 입력인 경우 카드번호 + ‘=’ + 유효기간 ('21' + '1234567890123456=1214')
    private byte[] iMonths;                                                                                 // 할부 개월       	일시불: 00, 그 외 할부 개월
    private byte[] dealAmount;                                                                              // 거래금액        	세금,봉사료 제외 금액
    private byte[] tax;                                                                                     // 세금
    private byte[] svcCharge;                                                                               // 봉사료
    private final byte[] currencyCd = StringUtil.getRPadSpace(3, "KRW").getBytes();             // 통화코드        	Default KRW
    private byte[] orgDealDt;                                                                               // 원거래일자      취소/직전취소인 경우:YYMMDD, 그 외 Space
    private byte[] orgApprovalNo;                                                                           // 원승인번호      취소/직전취소인 경우 Set
    private byte[] orgUniqueNo;                                                                             // 원거래고유번호  직전취소인 경우 승인 응답전문의 거래 고유번호 Seo// t
    private final byte[] fallbackInfo = StringUtil.getRPadSpace(3, "").getBytes();              // 폴백정보        	N:폴백거래+폴백사유코드(2Byte), Y:IC거래 + Space(2)
    private final byte[] billingCd = StringUtil.getRPadSpace(2, "").getBytes();                 // 결제코드        	결제코드 참조
    private final byte[] svcCd = StringUtil.getRPadSpace(8, "").getBytes();                     // 서비스코드      	미사용
    private final byte[] taxExempt = StringUtil.getLPadZero(9, "").getBytes();               // 비과세 금액     	결제 금액과 무관.  비과세 금액 표시에 사용
    private final byte[] cardPassword = StringUtil.getRPadSpace(18, "").getBytes();             // 비밀번호        	Key Index(2Byte; Number Type) + 암호화 PIN 데이터
    private final byte[] oilInfo = StringUtil.getRPadSpace(24, "").getBytes();                        // 주유정보        	NT:면세유+면세정보, OL:주유소/충전소+주유정보
    private final byte[] dBusinessNo = StringUtil.getRPadSpace(10, "").getBytes();                    // 하위사업자번호   PG Off-Line 거래인 경우 Set, 그 외 Space
    private final byte[] posSerialNo = StringUtil.getRPadSpace(16, "JTPOSDM16011E278").getBytes();    // POS 시리얼번호  	제이티넷에서 부여한 고유 일련번호, Default JTPOSDM16011E278
    private final byte[] additional1 = StringUtil.getRPadSpace(32, "").getBytes();                    // 부가정보1
    private final byte[] additional2 = StringUtil.getRPadSpace(128, "").getBytes();                   // 부가정보2
    private final byte[] reserved = StringUtil.getRPadSpace(64, "").getBytes();                       // Reserved    		Space Set
    private final byte[] signature = StringUtil.getRPadSpace(1, "").getBytes();                                                                                    // 서명처리        	Y: 서명사용, N:서명 미사용, R : 서명 재사용

    /**
     *
     * @param dealTypeCd    전문종류
     * @param terminalNo    단말기번호
     * @param wcc           WCC
     * @param transactionType        Track2
     * @param QRData        Track2
     * @param iMonths       할부
     * @param dealAmount    거래금액
     * @param tax           세금
     * @param svcCharge     봉사료
     * @param orgDealDt     원거래일자
     * @param orgApprovalNo 원승인일자
     * @param orgUniqueNo   원거래고유번호
     */
    public QRApproval(
            byte[] dealTypeCd,
            byte[] terminalNo,
            byte[] wcc,
            byte[] transactionType,
            byte[] QRData,
            byte[] iMonths,
            byte[] dealAmount,
            byte[] tax,
            byte[] svcCharge,
            byte[] orgDealDt,
            byte[] orgApprovalNo,
            byte[] orgUniqueNo
    ) {
        super(dealTypeCd, terminalNo);
        this.wcc = wcc;
        this.TransactionType = transactionType;
        this.QRData = QRData;
        this.iMonths = iMonths;
        this.dealAmount = dealAmount;
        this.tax = tax;
        this.svcCharge = svcCharge;
        this.orgDealDt = orgDealDt;
        this.orgApprovalNo = orgApprovalNo;
        this.orgUniqueNo = orgUniqueNo;
    }

    public byte[] getWcc() {
        return wcc;
    }

    public void setWcc(byte[] wcc) {
        this.wcc = wcc;
    }

    public byte[] getTrack2() {
        return QRData;
    }

    public void setTrack2(byte[] track2) {
        this.QRData = track2;
    }

    public byte[] getiMonths() {
        return iMonths;
    }

    public void setiMonths(byte[] iMonths) {
        this.iMonths = iMonths;
    }

    public byte[] getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(byte[] dealAmount) {
        this.dealAmount = dealAmount;
    }

    public byte[] getTax() {
        return tax;
    }

    public void setTax(byte[] tax) {
        this.tax = tax;
    }

    public byte[] getSvcCharge() {
        return svcCharge;
    }

    public void setSvcCharge(byte[] svcCharge) {
        this.svcCharge = svcCharge;
    }

    public byte[] getCurrencyCd() {
        return currencyCd;
    }

    public byte[] getOrgDealDt() {
        return orgDealDt;
    }

    public void setOrgDealDt(byte[] orgDealDt) {
        this.orgDealDt = orgDealDt;
    }

    public byte[] getOrgApprovalNo() {
        return orgApprovalNo;
    }

    public void setOrgApprovalNo(byte[] orgApprovalNo) {
        this.orgApprovalNo = orgApprovalNo;
    }

    public byte[] getOrgUniqueNo() {
        return orgUniqueNo;
    }

    public void setOrgUniqueNo(byte[] orgUniqueNo) {
        this.orgUniqueNo = orgUniqueNo;
    }

    public byte[] getFallbackInfo() {
        return fallbackInfo;
    }

    public byte[] getBillingCd() {
        return billingCd;
    }

    public byte[] getSvcCd() {
        return svcCd;
    }

    public byte[] getTaxExempt() {
        return taxExempt;
    }

    public byte[] getCardPassword() {
        return cardPassword;
    }

    public byte[] getOilInfo() {
        return oilInfo;
    }

    public byte[] getdBusinessNo() {
        return dBusinessNo;
    }

    public byte[] getPosSerialNo() {
        return posSerialNo;
    }

    public byte[] getAdditional1() {
        return additional1;
    }

    public byte[] getAdditional2() {
        return additional2;
    }

    public byte[] getReserved() {
        return reserved;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                super.create(),
                wcc,
                TransactionType,
                QRData,
                iMonths,
                dealAmount,
                tax,
                svcCharge,
                currencyCd,
                orgDealDt,
                orgApprovalNo,
                orgUniqueNo,
                fallbackInfo,
                billingCd,
                svcCd,
                taxExempt,
                cardPassword,
                oilInfo,
                dBusinessNo,
                posSerialNo,
                additional1,
                additional2,
                reserved,
                signature,
                cr
        );
    }
}
