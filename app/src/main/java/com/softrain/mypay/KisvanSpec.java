package com.softrain.mypay;

import android.content.Intent;
import android.os.Bundle;

public class KisvanSpec {
    public String inTestMode; // 테스트 모드를 저장하는 변수
    public String inTranCode; // 거래 코드를 저장하는 변수
    public String inPasswordMessage; // 비밀번호 메시지를 저장하는 변수
    public String inSecondTranCode; // 두 번째 거래 코드를 저장하는 변수
    public String inAgencyCode; // 기관 코드를 저장하는 변수
    public String inWCC; // WCC를 저장하는 변수
    public String inCardNo; // 카드 번호를 저장하는 변수
    public String inCardBin; // 카드 BIN을 저장하는 변수
    public String inSignFileName; // 서명 파일 이름을 저장하는 변수
    public String inInstallment; // 할부를 저장하는 변수
    public String inTotAmt; // 총 금액을 저장하는 변수
    public String inPinBlock; // PIN 블록을 저장하는 변수
    public String inVatAmt; // 부가가치세 금액을 저장하는 변수
    public String inSvcAmt; // 서비스 금액을 저장하는 변수
    public String inOrgAuthNo; // 원 승인 번호를 저장하는 변수
    public String inOrgAuthDate; // 원 승인 날짜를 저장하는 변수
    public String inSerialNo; // 일련번호를 저장하는 변수
    public String inCashAuthType; // 현금 승인 유형을 저장하는 변수
    public String inCheckNumber; // 수표 번호를 저장하는 변수
    public String inCheckIssueDate; // 수표 발행 날짜를 저장하는 변수
    public String inModelName; // 모델 이름을 저장하는 변수
    public String inCheckType; // 수표 유형을 저장하는 변수
    public String inCheckAccountNo; // 수표 계좌 번호를 저장하는 변수
    public String inSKTGoodsCode; // SKT 상품 코드를 저장하는 변수
    public String inPointGubun; // 포인트 구분을 저장하는 변수
    public String inBusinessNo; // 사업자 번호를 저장하는 변수
    public String inPasswdNo; // 비밀번호 번호를 저장하는 변수
    public String inIC_DeviceId; // IC 장치 ID를 저장하는 변수
    public String inLocalNo; // 로컬 번호를 저장하는 변수
    public String inVanKey; // VAN 키를 저장하는 변수
    public String inHospitalInfo; // 병원 정보를 저장하는 변수

    public String inKeyDownGubun; // 키 다운 구분을 저장하는 변수
    public String inVanId; // VAN ID를 저장하는 변수
    public String inCatId; // CAT ID를 저장하는 변수
    public String inCatIDGubun; // CAT ID 구분을 저장하는 변수
    public boolean inSimpleFlag; // 간편 모드 플래그를 저장하는 변수

    public String inBarcodeNumber; // 바코드 번호를 저장하는 변수
    public String inAdditionalInfo; // 추가 정보를 저장하는 변수

    public String inFiller3; // Filler 3을 저장하는 변수

    public String inOilInfo; // 유류 정보를 저장하는 변수
    public int inAccountCount = 0; // 계좌 수를 저장하는 변수
    public String[] inAccountInfo; // 계좌 정보를 저장하는 변수
    public String inPinData; // PIN 데이터를 저장하는 변수
    public int inAccountIndex = 0; // 계좌 인덱스를 저장하는 변수
    public String inIssuerCode; // 발급자 코드를 저장하는 변수
    public String inEncryptData; // 암호화된 데이터를 저장하는 변수
    public String inTrack3Data; // 트랙 3 데이터를 저장하는 변수
    public int inTimeOut = 30; // 타임아웃을 저장하는 변수

    public String inCatIP = ""; // CAT IP를 저장하는 변수
    public String inCatPort = ""; // CAT 포트를 저장하는 변수
    public boolean inConnectToCat = false; // CAT에 연결할지 여부를 저장하는 변수

    public byte[] inPrtData; // 프린트 데이터를 저장하는 변수

    public String reqGubun; // 요청 구분을 저장하는 변수

    public String outWorkingKey; // 작업 키를 저장하는 변수
    public String outCatId; // CAT ID를 저장하는 변수
    public String outReplyDate; // 응답 날짜를 저장하는 변수
    public String outReplyCode; // 응답 코드를 저장하는 변수
    public String outAuthNo; // 승인 번호를 저장하는 변수
    public String outIssuerCode; // 발급자 코드를 저장하는 변수
    public String outIssuerName; // 발급자 이름을 저장하는 변수
    public String outMerchantRegNo; // 가맹점 등록 번호를 저장하는 변수
    public String outMerchantName; // 가맹점 이름을 저장하는 변수
    public String outJanAmt; // Jan 금액을 저장하는 변수
    public String outAddedPoint; // 추가 포인트를 저장하는 변수
    public String outUsablePoint; // 사용 가능한 포인트를 저장하는 변수
    public String outTotalPoint; // 총 포인트를 저장하는 변수
    public String outDiscountPoint; // 할인 포인트를 저장하는 변수
    public String outUsedPoint; // 사용한 포인트를 저장하는 변수
    public String outReplyMsg1; // 응답 메시지 1을 저장하는 변수
    public String outReplyMsg2; // 응답 메시지 2를 저장하는 변수
    public String outCardNo; // 카드 번호를 저장하는 변수
    public String outCardBrand; // 카드 브랜드를 저장하는 변수
    public String outAccepterCode; // 가맹점 코드를 저장하는 변수
    public String outAccepterName; // 가맹점 이름을 저장하는 변수
    public String outAuthDate; // 승인 날짜를 저장하는 변수
    public String outBarcodeNumber; // 바코드 번호를 저장하는 변수
    public String outAccountNumber; // 계좌 번호를 저장하는 변수

    public String outTelephoneNo; // 전화 번호를 저장하는 변수
    public String outMerchantAddr; // 가맹점 주소를 저장하는 변수
    public String outChipName; // 칩 이름을 저장하는 변수
    public String outBusinessNo; // 사업자 번호를 저장하는 변수

    public String outWCC; // WCC를 저장하는 변수
    public String outInstallMent; // 할부를 저장하는 변수
    public String outTotAmt; // 총 금액을 저장하는 변수
    public String outVatAmt; // 부가가치세 금액을 저장하는 변수
    public String outSvcAmt; // 서비스 금액을 저장하는 변수
    public String outSignYn; // 서명 여부를 저장하는 변수
    public String outTradeNumber; // 거래 번호를 저장하는 변수
    public String outKeyDownDate; // 키 다운 날짜를 저장하는 변수

    public String outVanKey; // VAN 키를 저장하는 변수

    public String outPayGubun; // 결제 구분을 저장하는 변수
    public String outUserID; // 사용자 ID를 저장하는 변수
    public String outOTC; // OTC를 저장하는 변수
    public String outMemberShipBarcodeNumber; // 멤버십 바코드 번호를 저장하는 변수
    public String outPayType; // 결제 유형을 저장하는 변수
    public String outOrderNo; // 주문 번호를 저장하는 변수

    public String outPosSerialNo; // POS 시리얼 번호를 저장하는 변수
    public String outStatusICCard; // IC 카드 상태를 저장하는 변수

    public int outSignType; // 서명 유형을 저장하는 변수
    public int outSignDataLen; // 서명 데이터 길이를 저장하는 변수
    public String outSignFilePath; // 서명 파일 경로를 저장하는 변수

    public String outSafeCardICData; // 안전한 카드 IC 데이터를 저장하는 변수

    public String outFiller; // 채우기 변수
    public String[] outAccountInfo; // 계좌 정보 배열을 저장하는 변수
    public String outCardBin; // 카드 BIN을 저장하는 변수

    public KisvanSpec()
    {
        Init();
    }

    public void Init()
    {
        inTestMode = "";

        inTranCode = "";
        inPasswordMessage = "";
        inSecondTranCode = "";
        inAgencyCode = "";
        inWCC = "";
        inSerialNo = "";

        inCardNo = "";
        inCardBin = "";
        inSignFileName = "";
        inInstallment = "";
        inTotAmt = "";
        inCatId = "";

        inPinBlock = "";
        inVatAmt = "";
        inSvcAmt = "";
        inOrgAuthNo = "";
        inOrgAuthDate = "";
        inCashAuthType = "";

        inCheckNumber = "";
        inCheckIssueDate = "";
        inCheckType = "";
        inCheckAccountNo = "";
        inSKTGoodsCode = "";
        inFiller3 = "";
        inPointGubun = "";
        inBusinessNo = "";
        inPasswdNo = "";
        inIC_DeviceId = "";
        inLocalNo = "";
        inVanKey = "";
        inHospitalInfo = "";

        inKeyDownGubun = "";
        inVanId = "";

        inSimpleFlag = false;

        inBarcodeNumber = "";
        inOilInfo = "";
        reqGubun = "";
        inAdditionalInfo = "";
        inPinData = "";
        inAccountIndex = 0;
        inIssuerCode = "";
        inEncryptData = "";
        inTrack3Data = "";
        inTimeOut = 30;

        inCatIP = "";
        inCatPort = "";
        inPrtData = null;
        inConnectToCat = false;

        outSafeCardICData = "";
        outCardBin = "";

        outWorkingKey = "";
        outCatId = "";
        outReplyDate = "";
        outReplyCode = "";
        outAuthNo = "";
        outIssuerCode = "";
        outIssuerName = "";
        outMerchantRegNo = "";
        outMerchantName = "";
        outJanAmt = "";
        outAddedPoint = "";
        outUsablePoint = "";
        outTotalPoint = "";
        outUsedPoint = "";
        outDiscountPoint = "";
        outReplyMsg1 = "";
        outReplyMsg2 = "";
        outCardNo = "";
        outCardBrand = "";
        outAccepterCode = "";
        outAccepterName = "";
        outAuthDate = "";
        outVanKey = "";

        outTelephoneNo = "";
        outMerchantAddr = "";
        outChipName = "";
        outBusinessNo = "";

        outWCC = "";
        outInstallMent = "";
        outTotAmt = "";
        outVatAmt = "";
        outSvcAmt = "";

        outSignYn = "";
        outTradeNumber = "";
        outKeyDownDate = "";

        outAccountNumber = "";

        outPayGubun = "";
        outUserID = "";
        outOTC = "";
        outMemberShipBarcodeNumber = "";
        outPayType = "";
        outOrderNo = "";

        outPosSerialNo = "";
        outStatusICCard = "";
        outFiller = "";
    }

    public void RequestData(Bundle bundle) {
        bundle.putString("inTestMode", inTestMode);
        bundle.putString("inTranCode", inTranCode);
        bundle.putString("inPasswordMessage", inPasswordMessage);
        bundle.putString("inSecondTranCode", inSecondTranCode);
        bundle.putString("inSerialNo", inSerialNo);
        bundle.putString("inAgencyCode", inAgencyCode);
        bundle.putString("inWCC", inWCC);
        bundle.putString("inCardNo", inCardNo);
        bundle.putString("inCardBin", inCardBin);
        bundle.putString("inSignFileName", inSignFileName);
        bundle.putString("inInstallment", inInstallment);
        bundle.putString("inTotAmt", inTotAmt);
        bundle.putString("inCatId", inCatId);
        bundle.putString("inCatIDGubun", inCatIDGubun);
        bundle.putString("inPinBlock", inPinBlock);
        bundle.putString("inVatAmt", inVatAmt);
        bundle.putString("inSvcAmt", inSvcAmt);
        bundle.putString("inOrgAuthNo", inOrgAuthNo);
        bundle.putString("inOrgAuthDate", inOrgAuthDate);
        bundle.putString("inCashAuthType", inCashAuthType);
        bundle.putString("inCheckNumber", inCheckNumber);
        bundle.putString("inCheckIssueDate", inCheckIssueDate);
        bundle.putString("inModelName", inModelName);
        bundle.putString("inCheckType", inCheckType);
        bundle.putString("inCheckAccountNo", inCheckAccountNo);
        bundle.putString("inSKTGoodsCode", inSKTGoodsCode);
        bundle.putString("inFiller3", inFiller3);
        bundle.putString("inPointGubun", inPointGubun);
        bundle.putString("inBusinessNo", inBusinessNo);
        bundle.putString("inPasswdNo", inPasswdNo);
        bundle.putString("inIC_DeviceId", inIC_DeviceId);
        bundle.putString("inLocalNo", inLocalNo);
        bundle.putString("inVanKey", inVanKey);
        bundle.putString("inHospitalInfo", inHospitalInfo);
        bundle.putString("inAdditionalInfo", inAdditionalInfo);

        bundle.putString("inKeyDownGubun", inKeyDownGubun);
        bundle.putString("inVanId", inVanId);

        bundle.putBoolean("inSimpleFlag", inSimpleFlag);

        bundle.putString("inBarcodeNumber", inBarcodeNumber);
        bundle.putString("inOilInfo", inOilInfo);
        bundle.putString("inPinData", inPinData);
        bundle.putInt("inAccountIndex", inAccountIndex);

        bundle.putString("inPosSerialNo", "1");

        bundle.putInt("inAccountCount", inAccountCount);
        bundle.putStringArray("inAccountInfo", inAccountInfo);

        bundle.putString("reqGubun", reqGubun);

        bundle.putString("inIssuerCode", inIssuerCode);
        bundle.putString("inEncryptData", inEncryptData);
        bundle.putString("inTrack3Data", inTrack3Data);
        bundle.putInt("inTimeOut", inTimeOut);

        bundle.putString("inCatIP", inCatIP);
        bundle.putString("inCatPort", inCatPort);
        bundle.putByteArray("inPrtData", inPrtData);
        bundle.putBoolean("inConnectToCat", inConnectToCat);
    }

    public void RequestData(Intent intent) {
        intent.putExtra("inTestMode", inTestMode);
        intent.putExtra("inTranCode", inTranCode);
        intent.putExtra("inPasswordMessage", inPasswordMessage);
        intent.putExtra("inSecondTranCode", inSecondTranCode);
        intent.putExtra("inSerialNo", inSerialNo);
        intent.putExtra("inAgencyCode", inAgencyCode);
        intent.putExtra("inWCC", inWCC);
        intent.putExtra("inCardNo", inCardNo);
        intent.putExtra("inSignFileName", inSignFileName);
        intent.putExtra("inInstallment", inInstallment);
        intent.putExtra("inTotAmt", inTotAmt);
        intent.putExtra("inCatId", inCatId);
        intent.putExtra("inCatIDGubun", inCatIDGubun);
        intent.putExtra("inPinBlock", inPinBlock);
        intent.putExtra("inVatAmt", inVatAmt);
        intent.putExtra("inSvcAmt", inSvcAmt);
        intent.putExtra("inOrgAuthNo", inOrgAuthNo);
        intent.putExtra("inOrgAuthDate", inOrgAuthDate);
        intent.putExtra("inCashAuthType", inCashAuthType);
        intent.putExtra("inCheckNumber", inCheckNumber);
        intent.putExtra("inCheckIssueDate", inCheckIssueDate);
        intent.putExtra("inModelName", inModelName);
        intent.putExtra("inCheckType", inCheckType);
        intent.putExtra("inCheckAccountNo", inCheckAccountNo);
        intent.putExtra("inSKTGoodsCode", inSKTGoodsCode);
        intent.putExtra("inFiller3", inFiller3);
        intent.putExtra("inPointGubun", inPointGubun);
        intent.putExtra("inBusinessNo", inBusinessNo);
        intent.putExtra("inPasswdNo", inPasswdNo);
        intent.putExtra("inIC_DeviceId", inIC_DeviceId);
        intent.putExtra("inLocalNo", inLocalNo);
        intent.putExtra("inVanKey", inVanKey);
        intent.putExtra("inHospitalInfo", inHospitalInfo);
        intent.putExtra("inAdditionalInfo", inAdditionalInfo);

        intent.putExtra("inKeyDownGubun", inKeyDownGubun);
        intent.putExtra("inVanId", inVanId);

        intent.putExtra("inSimpleFlag", inSimpleFlag);

        intent.putExtra("inBarcodeNumber", inBarcodeNumber);
        intent.putExtra("inOilInfo", inOilInfo);

        intent.putExtra("inPosSerialNo", "1");

        intent.putExtra("inAccountCount", inAccountCount);
        intent.putExtra("inAccountInfo", inAccountInfo);

        intent.putExtra("reqGubun", reqGubun);
        intent.putExtra("inTimeOut", inTimeOut);

        intent.putExtra("inCatIP", inCatIP);
        intent.putExtra("inCatPort", inCatPort);
        intent.putExtra("inConnectToCat", inConnectToCat);
    }

    public void ResponseData(Intent intent) {
        outReplyCode = intent.getStringExtra("outReplyCode");
        outReplyMsg1 = intent.getStringExtra("outReplyMsg1");
        outReplyMsg2 = intent.getStringExtra("outReplyMsg2");

        outWorkingKey = intent.getStringExtra("outWorkingKey");
        outCatId = intent.getStringExtra("outCatId");
        outReplyDate = intent.getStringExtra("outReplyDate");

        outAuthNo = intent.getStringExtra("outAuthNo");
        outIssuerCode = intent.getStringExtra("outIssuerCode");
        outIssuerName = intent.getStringExtra("outIssuerName");
        outMerchantRegNo = intent.getStringExtra("outMerchantRegNo");
        outMerchantName = intent.getStringExtra("outMerchantName");
        outJanAmt = intent.getStringExtra("outJanAmt");
        outAddedPoint = intent.getStringExtra("outAddedPoint");
        outUsablePoint = intent.getStringExtra("outUsablePoint");
        outTotalPoint = intent.getStringExtra("outTotalPoint");
        outDiscountPoint = intent.getStringExtra("outDiscountPoint");
        outUsedPoint = intent.getStringExtra("outUsedPoint");

        outCardNo = intent.getStringExtra("outCardNo");
        outAccepterCode = intent.getStringExtra("outAccepterCode");
        outAccepterName = intent.getStringExtra("outAccepterName");
        outAuthDate = intent.getStringExtra("outAuthDate");
        outBarcodeNumber = intent.getStringExtra("outBarcodeNumber");

        outVanKey = intent.getStringExtra("outVanKey");

        outTelephoneNo = intent.getStringExtra("outTelephoneNo");
        outMerchantAddr = intent.getStringExtra("outMerchantAddr");
        outChipName = intent.getStringExtra("outChipName");
        outBusinessNo = intent.getStringExtra("outBusinessNo");

        outWCC = intent.getStringExtra("outWCC");
        outInstallMent = intent.getStringExtra("outInstallMent");
        outTotAmt = intent.getStringExtra("outTotAmt");
        outVatAmt = intent.getStringExtra("outVatAmt");
        outSvcAmt = intent.getStringExtra("outSvcAmt");
        outSignYn = intent.getStringExtra("outSignYn");
        outTradeNumber = intent.getStringExtra("outTradeNumber");
        outCardBrand = intent.getStringExtra("outCardBrand");
        outAccountNumber = intent.getStringExtra("outAccountNumber");
        outKeyDownDate = intent.getStringExtra("outKeyDownDate");

        outPayGubun = intent.getStringExtra("outPayGubun");
        outUserID = intent.getStringExtra("outUserID");
        outOTC = intent.getStringExtra("outOTC");
        outMemberShipBarcodeNumber = intent.getStringExtra("outMemberShipBarcodeNumber");
        outOrderNo = intent.getStringExtra("outOrderNo");
        outPayType = intent.getStringExtra("outPayType");

        outPosSerialNo = intent.getStringExtra("outPosSerialNo");
        outStatusICCard = intent.getStringExtra("outStatusICCard");

        outSignType = intent.getIntExtra("outSignType", 0);
        outSignDataLen = intent.getIntExtra("outSignDataLen", 0);
        outSignFilePath = intent.getStringExtra("outSignFilePath");
        outSafeCardICData = intent.getStringExtra("outSafeCardICData");
        outCardBin = intent.getStringExtra("outCardBin");
        outFiller = intent.getStringExtra("outFiller");
    }

}
