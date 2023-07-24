package com.softrain.mypay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.CrashUtils;
import com.softrain.mypay.utils.BarCode128Utils;
import com.softrain.mypay.utils.PrintImageUtils;
import com.softrain.mypay.utils.StatusDescribe;
import com.szsicod.print.escpos.PrinterAPI;
import com.szsicod.print.io.InterfaceAPI;
import com.szsicod.print.io.SerialAPI;
import com.szsicod.print.log.AndroidLogCatStrategy;
import com.szsicod.print.log.Logger;
import com.szsicod.print.log.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

public class PrintFactory {

    // 시리얼 포트의 이름을 저장
    private static final String SERIAL_PORT_NAME = "/dev/ttyS3";
    private static PrinterAPI mPrinter;
    // 시리얼 통신 속도
    private static final int SERIAL_BAUDRATE = 38400;

    static {
        System.loadLibrary("usb1.0");
        System.loadLibrary("serial_icod");
        System.loadLibrary("image_icod");
    }

    public static void init(Context context) {
        //  클래스는 여러 가지 유틸리티 메소드를 제공
        Utils.init(context);
        // 프린터의 출력 기능을 활성화
        PrinterAPI.getInstance().setOutput(true);
        // 로그캣에 로그를 출력
        Logger.addLogStrategy(new AndroidLogCatStrategy());
        // 클래스의 인스턴스를 반환
        mPrinter = PrinterAPI.getInstance();
        // 앱에서 발생하는 예외 상황을 처리하는 유틸리티 클래스, 인자로는 앱의 외부 저장소 경로를 전달
        CrashUtils.init(Environment.getExternalStorageDirectory().toString() + File.separator + "icod" + File.separator);
        // 프린터와 연결
        funcPrinterConnect();
    }

    // 프린터 연결 메소드
    public static void funcPrinterConnect() {
                if (mPrinter.isConnect()) {
                    mPrinter.disconnect();
                }
                InterfaceAPI io = null;
                io = new SerialAPI(new File(SERIAL_PORT_NAME),
                        SERIAL_BAUDRATE,
                        0);
                if (io != null) {
                    mPrinter.connect(io);
                }
    }

    public static void funcPrintText(final String rReceiptPageNum, final String rStore, final String rReceipt, final String rCard, final String rBill) {
                try {
                    if(rReceiptPageNum.equals("2")){
                        mPrinter.setAlignMode(1); // 0왼쪽, 1가운데, 2오른쪽
                        mPrinter.chineseFontSet(1); //0영어, 1한국어, 2일본어
                        mPrinter.fontSizeSet(2); // 폰트 크키 설정
                        mPrinter.setEnableUnderLine(0); // 밑줄사용 여부 0:사용안함
                        mPrinter.setCharSize(1,1); // 문자 크기를 설정
                        mPrinter.printString("[메뉴 교환권]", "CP949", true);
                        mPrinter.setAlignMode(0); // 0왼쪽, 1가운데, 2오른쪽
                        mPrinter.printAndFeedLine(1); // 프린터로 1줄을 출력하고, 바로 다음 줄로 이동
                        mPrinter.fontSizeSet(0);  // 폰트 크키 설정
                        mPrinter.setCharSize(0,0); // 문자 크기를 설정
                        String couponNumber = String.format("%s%s",
                                convert("교환권 번호", 13),
                                convert(": "+"0157", 1));
                        mPrinter.printString(couponNumber, "CP949", true);
                        String couponDate = String.format("%s%s",
                                convert("일시", 13),
                                convert(": "+"05/04 11:43:22", 1));
                        mPrinter.printString(couponDate, "CP949", true);
                        mPrinter.printString("-----------------------------------------------", "CP949", true);
                        String couponCategories = String.format("%s%s",
                                convert("상품명", 40),
                                convert("수량", 5));
                        mPrinter.printString(couponCategories, "CP949", true);
                        mPrinter.printString("-----------------------------------------------", "CP949", true);
                        JSONArray jaCouponData = new JSONArray(rReceipt);
                        for (int i = 0; i < jaCouponData.length(); i++) {
                            JSONObject joCouponData = jaCouponData.getJSONObject(i);
                            // JSON 객체에서 필요한 값을 추출
                            String name = joCouponData.getString("name");
                            int quantity = joCouponData.getInt("quantity");
                            StringBuilder sbCoupon = new StringBuilder();
                            sbCoupon.append(convert(name, 42));
                            sbCoupon.append(convert(String.valueOf(quantity), 5));
                            mPrinter.printString(sbCoupon.toString(), "CP949", true);
                        }
                        mPrinter.printString("-----------------------------------------------", "CP949", true);
                        mPrinter.printAndFeedLine(3);
                        mPrinter.halfCut();
                    }

                    JSONObject joStore = new JSONObject(rStore);
                    String sName = joStore.getString("sName");
                    String sAddress = joStore.getString("sAddress");
                    String sRepresentative = joStore.getString("sRepresentative");
                    String sTel = joStore.getString("sTel");
                    String sOrderDate = joStore.getString("sOrderDate");
                    String sOrderNumber = joStore.getString("sOrderNumber");

                    JSONObject joBill = new JSONObject(rBill);
                    String sTotal = joBill.getString("total");
                    String sTax = joBill.getString("tax");
                    Log.e("총 값", " : " + sTotal);
                    Log.e("총 세 ", " : " + sTax);
                    // 텍스트 스타일 설정
                    mPrinter.setAlignMode(1); // 0왼쪽, 1가운데, 2오른쪽
                    mPrinter.chineseFontSet(1); //0영어, 1한국어, 2일본어
                    mPrinter.fontSizeSet(2); // 폰트 크키 설정
                    mPrinter.setEnableUnderLine(0); // 밑줄사용 여부 0:사용안함
                    mPrinter.setCharSize(1,1);  // 문자 크기를 설정
                    mPrinter.printString("[영수증]", "CP949", true);
                    mPrinter.printAndFeedLine(1);
                    mPrinter.setAlignMode(0);
                    mPrinter.fontSizeSet(0);
                    mPrinter.setCharSize(0,0);
                    String storeName = String.format("%s%s",
                            convert("매 장 명", 10),
                            convert(sName, 30));
                    mPrinter.printString(storeName, "CP949", true);
                    String storeAddress = String.format("%s%s",
                            convert("매장주소", 10),
                            convert(sAddress, 30));
                    mPrinter.printString(storeAddress, "CP949", true);
                    String storeRepresentative = String.format("%s%s",
                            convert("대표자", 10),
                            convert(sRepresentative, 30));
                    mPrinter.printString(storeRepresentative, "CP949", true);
                    String storeRepresentativeNumber = String.format("%s%s",
                            convert("TEL", 10),
                            convert(sTel, 30));
                    mPrinter.printString(storeRepresentativeNumber, "CP949", true);
                    String storeOrderDate = String.format("%s%s",
                            convert("주문일시", 10),
                            convert(sOrderDate, 30));
                    mPrinter.printString(storeOrderDate, "CP949", true);
                    String storeOrderNumber = String.format("%s%s",
                            convert("주문번호", 10),
                            convert(sOrderNumber, 30));
                    mPrinter.printString(storeOrderNumber, "CP949", true);
                    mPrinter.printString("-----------------------------------------------", "CP949", true);
                    String storeCategories = String.format("%s%s%s%s",
                            convert("상품명", 23),
                            convert("단가", 8),
                            convert("수량", 8),
                            convert("금액", 8));
                    mPrinter.printString(storeCategories, "CP949", true);
                    mPrinter.printString("-----------------------------------------------", "CP949", true);
                    JSONArray jaStoreData = new JSONArray(rReceipt);
                    for (int i = 0; i < jaStoreData.length(); i++) {
                        JSONObject joStoreData = jaStoreData.getJSONObject(i);
                        // JSON 객체에서 필요한 값을 추출
                        String name = joStoreData.getString("name");
                        String price = joStoreData.getString("price");
                        int quantity = joStoreData.getInt("quantity");
                        int amount = joStoreData.getInt("quantity") * joStoreData.getInt("price");
                        StringBuilder sbStoreData = new StringBuilder();
                        sbStoreData.append(convert(name, 22));
                        sbStoreData.append(convert(formatPrice(Integer.parseInt(price)), 10));
                        sbStoreData.append(convert(String.valueOf(quantity), 7));
                        sbStoreData.append(convert(formatPrice(amount), 8));
                        mPrinter.printString(sbStoreData.toString(), "CP949", true);
                    }
                    mPrinter.printString("-----------------------------------------------", "CP949", true);
                    String storeMoney = String.format("%s%s",
                            convert("공급가액", 39),
                            convert(formatPrice(Integer.parseInt(sTotal)), 5));
                    mPrinter.printString(storeMoney, "CP949", true);
                    String storeSurtax = String.format("%s%s",
                            convert("부 가 세", 39),
                            convert(formatPrice(Integer.parseInt(sTax)), 5));
                    mPrinter.printString(storeSurtax, "CP949", true);
                    mPrinter.printString("-----------------------------------------------", "CP949", true);
                    int total = Integer.parseInt(sTotal);
                    int tax = Integer.parseInt(sTax);
                    int result = total + tax;
                    String storeResult = String.format("%s%s",
                            convert("합계", 39),
                            convert(formatPrice(Integer.parseInt(String.valueOf(result))), 5));
                    mPrinter.printString(storeResult, "CP949", true);
                    mPrinter.printString("-----------------------------------------------", "CP949", true);
                    JSONArray jaCardInfo = new JSONArray(rCard);
                    for (int i = 0; i < jaCardInfo.length(); i++) {
                        mPrinter.setAlignMode(1); // 0왼쪽, 1가운데, 2오른쪽
                        mPrinter.printString("<<< 신 용 승 인 정 보 >>>", "CP949", true);
                        mPrinter.setAlignMode(0);
                        JSONObject joCardInfo = jaCardInfo.getJSONObject(i);
                        // JSON 객체에서 필요한 값을 추출
                        String rIssuingCompany = joCardInfo.getString("issuingCompany");
                        String rCardNumber = joCardInfo.getString("cardNumber");
                        String rApprovalNumber = joCardInfo.getString("approvalNumber");
                        String rApprovalDate = joCardInfo.getString("approvalDate");
                        String rMerchantNumber = joCardInfo.getString("merchantNumber");
                        String cardCompanyName = String.format("%s%s",
                                convert2("카드종류  ", 8),
                                convert(" : "+rIssuingCompany, 30));
                        mPrinter.printString(cardCompanyName, "CP949", true);
                        String cardNumber = String.format("%s%s",
                                convert2("카드번호  ", 8),
                                convert(" : "+rCardNumber, 30));
                        mPrinter.printString(cardNumber, "CP949", true);
                        String installment = String.format("%s%s",
                                convert2("할부개월  " , 8),
                                convert(" : "+"일시불  ", 30));
                        mPrinter.printString(installment, "CP949", true);
                        String approvalMoney = String.format("%s%s",
                                convert2("승인금액  ", 8),
                                convert(" : "+formatPrice(result / jaCardInfo.length()), 30));
                        mPrinter.printString(approvalMoney, "CP949", true);
                        String approvalNumber = String.format("%s%s",
                                convert2("승인번호  ", 8),
                                convert(" : "+rApprovalNumber, 30));
                        mPrinter.printString(approvalNumber, "CP949", true);
                        String approvalDate = String.format("%s%s",
                                convert2("승인일시  ", 8),
                                convert(" : "+rApprovalDate, 30));
                        mPrinter.printString(approvalDate, "CP949", true);
                        String purchaseCompany = String.format("%s%s",
                                convert2("가맹점번호", 8),
                                convert(" : "+rMerchantNumber, 30));
                        mPrinter.printString(purchaseCompany, "CP949", true);
                    }
                    mPrinter.printString("-----------------------------------------------", "CP949", true);
                    mPrinter.cutPaper(66, 0); // 용지 자르기
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }

    // 전각문자 개수를 세주는 메서드
    private static int getKorCnt(String kor) {
        int cnt = 0;
        for (int i = 0; i < kor.length(); i++) {
            if (kor.charAt(i) >= '가' && kor.charAt(i) <= '힣') {
                cnt++;
            }
        }
        return cnt;
    }

    // 한국 돈 표기법으로 형식화된 문자열 반환
    public static String formatPrice(int price) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        return decimalFormat.format(price);
    }

    // 전각문자의 개수만큼 문자열 길이를 빼주는 메서드
    public static String convert(String word, int size) {
        String formatter = String.format("%%-%ds", size - getKorCnt(word));
        return String.format(formatter, word);
    }

    // 전각문자의 개수만큼 문자열 길이를 빼주는 메서드
    public static String convert2(String word, int size) {
        String formatter = String.format("%%%ds", size - getKorCnt(word));
        return String.format(formatter, word);
    }

    // 바코드를 생성메소드
    public void bardCodePrint() {
        String barStr = "1234567890";
        mPrinter.sendOrder(BarCode128Utils.gainBarCode128(barStr,"A"));
    }

    // 이미지 출력
    public void imgPrint() {
//        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.face);
//        int nWidth = 384;
//        int nBinaryAlgorithm = 0;
//        byte[] data = PrintImageUtils.POS_PrintPicture(mBitmap, nWidth, nBinaryAlgorithm);
//        mPrinter.sendOrder(data);
    }

    // 프린터 상태정보
    public void statuesPrint() {
        String stateStr = StatusDescribe.getStatusDescribe(mPrinter.getStatus());
    }

    // QRcode
    public void qrCodePrint() {
        String qrStr = "http://www.szicod.com/";
        mPrinter.printQRCode(qrStr, 10, false);
        mPrinter.cutPaper(66, 0);
    }

    public static void printerDestroy() {
        mPrinter.disconnect();
    }
}


