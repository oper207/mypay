package com.softrain.mypay;

import static net.posprinter.POSConnect.createDevice;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.softrain.mypay.XprinterUtils.HexUtils;

import net.posprinter.IDeviceConnection;
import net.posprinter.POSConnect;
import net.posprinter.POSConst;
import net.posprinter.POSPrinter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;

public class XPrintFactory {

    // POS 프린터 연결을 위한 IDeviceConnection 객체
    private IDeviceConnection connect;
    // POS 프린터 객체
    private POSPrinter printer;
    // 영수증 싸인 변수 값
    private String outSignFilePath; // Add this variable
    // 영수증 중복 출력 방지
    boolean isStartingReceipt = false;
    // 영수증 상태 값
    private String receiptState = null;

    public void xInit(Context context) {
        POSConnect.init(context);
        isStartingReceipt = false;
    }

    // Add an interface for the callback
    public interface ConnectionStatusCallback {
        void onConnectionStatusChanged(String status);
    }

    public boolean isPrinterConnected() {
        return connect != null && connect.isConnect();
    }

    public void setOutSignFilePath(String filePath) {
        outSignFilePath = filePath;
    }

    // 프린터 연결을 수행하는 메서드
    public void connect(String mac, ConnectionStatusCallback callback) {
        TextLog.o(" " + ConstDef.MYPAYT_PRINT_CONNECTION_STATUS + " Status: " + connect);
        if (connect != null) {
            connect.close();
        }

        connect = createDevice(POSConnect.DEVICE_TYPE_USB);
        connect.connect(null, (code, msg) -> {
            if (code == POSConnect.CONNECT_SUCCESS) {
                // 연결 성공 시
                printer = new POSPrinter(connect);
                connect.startReadLoop(this::receiveData);
                TextLog.o(" " + ConstDef.MYPAYT_PRINT_CONNECTION_STATUS + " Connect: " + connect);
                receiptState = "success";
                callback.onConnectionStatusChanged(receiptState);
            } else if (code == POSConnect.CONNECT_FAIL) {
                // 연결 실패 시
                connect.close();
                TextLog.o(" " + ConstDef.MYPAYT_PRINT_CONNECTION_STATUS + " Connection failed: " + connect);
                receiptState = "faild";
                callback.onConnectionStatusChanged(receiptState);
            } else if (code == POSConnect.CONNECT_INTERRUPT) {
                // 연결 중단 시
                connect.close();
                TextLog.o(" " + ConstDef.MYPAYT_PRINT_CONNECTION_STATUS + " Disconnect: " + connect);
                receiptState = "disconnect";
                callback.onConnectionStatusChanged(receiptState);
            }
        });
    }

    // 데이터 수신 처리 메서드
    private void receiveData(byte[] data) {
        String hexString = HexUtils.bytes2HexStrWithSpace(data, data.length);
        // 사용할 코드로 처리하세요
        // (HexUtils.bytes2String(data))
    }

    public void XprintReceipt(String rReceiptPageNum, String rStore, String rReceipt, String rCard, String rBill, String rReceiptType) {
        if(isStartingReceipt) {
            return;
        }
        isStartingReceipt = true;
        if(printer != null) {
            TextLog.o(" " + ConstDef.MYPAYT_PRINT_OUTPUT_VALUES + " rReceiptPageNum: " + rReceiptPageNum + " rStore: " + rStore + " rReceipt: " + rReceipt + " rCard: " + rCard + " rBill: " + rBill + " outSignFilePath: " + outSignFilePath);
            try {
                // 매장정보
                JSONObject joStore = new JSONObject(rStore);
                String sName = joStore.optString("sName", null);
                String sAddress = joStore.optString("sAddress", null);
                String sRepresentative = joStore.optString("sRepresentative", null);
                String sTel = joStore.optString("sTel", null);
                String sOrderDate = joStore.optString("sOrderDate", null);
                String sOrderNumber = joStore.optString("sOrderNumber", null);

                printer.setCharSet("CP949");
                // 주문번호가 필요 시
                if(rReceiptType.equals("outputOrderNumber")){
                    printer.printText("[주문번호]\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, 2).feedLine();
                    String couponNumber = String.format("%s%s",
                            convert("주문번호", 13),
                            convert(": "+rReceiptPageNum, 13));
                    printer.printString(couponNumber).feedLine();
                    String couponDate = String.format("%s%s",
                            convert("일시", 13),
                            convert(": "+sOrderDate, 1));
                    printer.printString(couponDate).feedLine();
                    printer.printString("-----------------------------------------------").feedLine();
                    String couponCategories = String.format("%s%s",
                            convert("상품명", 40),
                            convert("수량", 5));
                    printer.printString(couponCategories).feedLine();
                    printer.printString("-----------------------------------------------").feedLine();
                    JSONArray jaCouponData = new JSONArray(rReceipt);
                    for (int i = 0; i < jaCouponData.length(); i++) {
                        JSONObject joCouponData = jaCouponData.getJSONObject(i);
                        // JSON 객체에서 필요한 값을 추출
                        String name = joCouponData.getString("name");
                        int quantity = joCouponData.getInt("quantity");
                        StringBuilder sbCoupon = new StringBuilder();
                        String shortenedName = shortenString(name, 15); // 최대 길이 10로 설정
                        sbCoupon.append(convert(shortenedName, 42));
                        sbCoupon.append(convert(String.valueOf(quantity), 5));
                        printer.printString(sbCoupon.toString()).feedLine();
                    }
                    printer.printString("-----------------------------------------------").feedLine(5);
                    printer.cutPaper();
                }
                else {
                    // 매장정보 세팅
                    printer.initializePrinter().printText("[영수증]\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, 2).feedLine();
                    String[][] storeInfoData = {
                            {"매 장 명", sName},
                            {"매장주소", sAddress},
                            {"대표자", sRepresentative},
                            {"TEL", sTel},
                            {"주문일시", sOrderDate},
                            {rReceiptType, sOrderNumber}
                    };
                    for (String[] info : storeInfoData) {
                        String formattedInfo = String.format("%s%s", convert(info[0], 10), convert(info[1], 30));
                        printer.printString(formattedInfo).feedLine();
                    }
                    printer.printString("-----------------------------------------------").feedLine();
                    // 매장정보 세팅 end

                    JSONArray jaStoreData = new JSONArray(rReceipt);
                    JSONObject joDataLenth = new JSONObject(String.valueOf(jaStoreData.getJSONObject(0)));
                    int purchaseMenuOutputStatus = joDataLenth.length();

                    // 음식 목록 있는 경우
                    if(purchaseMenuOutputStatus > 1) {
                        // 음식 목록 세팅하는 곳
                        String storeCategories = String.format("%s%s%s%s",
                                convert("상품명", 23),
                                convert("단가", 8),
                                convert("수량", 8),
                                convert("금액", 9));
                        printer.printTextAttribute(storeCategories, POSConst.FNT_BOLD).printString("-----------------------------------------------").feedLine();

                    }

                    for (int i = 0; i < jaStoreData.length(); i++) {
                        // JSON 객체에서 필요한 값을 추출
                        JSONObject joStoreData = jaStoreData.getJSONObject(i);
                        StringBuilder sbStoreData = new StringBuilder();
                        // 음식 목록 있는 경우
                        if(purchaseMenuOutputStatus > 1) {
                            String price = joStoreData.getString("price");
                            String name = joStoreData.getString("name");
                            int quantity = joStoreData.getInt("quantity");
                            int amount = quantity * Integer.parseInt(price);
                            String shortenedName = shortenString(name, price.equals("0") ? 15 : 10);
                            sbStoreData.append(convert(shortenedName, 22));
                            sbStoreData.append(price.equals("0") ? convert("", 10) : convert(formatPrice(Integer.parseInt(price)), 10));
                            sbStoreData.append(convert(String.valueOf(quantity), 7));
                            sbStoreData.append(amount == 0 ? convert("", 9) : convert(formatPrice(amount), 9));
                            printer.printString(sbStoreData.toString());
                        }
                        // 음식 목록이 없는 경우
                        else {
                            String name = joStoreData.getString("name");
                            String shortenedName = shortenString(name, 40);
                            sbStoreData.append(convert(shortenedName, 48));
                            printer.printString(sbStoreData.toString());
                            break;
                        }
                    }
                    printer.printString("-----------------------------------------------").feedLine();
                    // 음식 목록 세팅하는 곳 end

                    // 돈 세금 합계 세팅
                    JSONObject joBill = new JSONObject(rBill);
                    String sTotal = joBill.getString("total");
                    String sTax = joBill.getString("tax");

                    String storeMoney = String.format("%s%s",
                            convert("공급가액", 39),
                            convert(formatPrice(Integer.parseInt(sTotal)-Integer.parseInt(sTax)), 9));
                    printer.printString(storeMoney);

                    String storeSurtax = String.format("%s%s",
                            convert("부 가 세", 39),
                            convert(formatPrice(Integer.parseInt(sTax)), 9));
                    printer.printString(storeSurtax).printString("-----------------------------------------------").feedLine();

                    String storeResult = String.format("%s%s",
                            convert("합계", 39),
                            convert(formatPrice(Integer.parseInt(sTotal)), 9));
                    printer.printString(storeResult).printString("-----------------------------------------------").feedLine();
                    // 돈 세금 합계 세팅 end

                    // 카드 정보 세팅
                    JSONArray jaCardInfo = new JSONArray(rCard);
                    for (int i = 0; i < jaCardInfo.length(); i++) {
                        JSONObject joCardInfo = jaCardInfo.getJSONObject(i);
                        // JSON 객체에서 필요한 값을 추출
                        String rIssuingCompany = joCardInfo.getString("issuingCompany");
                        String rCardNumber = joCardInfo.getString("cardNumber");
                        String rApprovalNumber = joCardInfo.getString("approvalNumber");
                        String rApprovalDate = joCardInfo.getString("approvalDate");
                        String rApprovalMoney = joCardInfo.getString("approvalMoney");
                        // 현금 영수증
                        if(rIssuingCompany.equals("cashReceipt")) {
                            printer.printText("<<< 현금영수증 >>>\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_1HEIGHT);

                            String cardNumber = String.format("%s%s", convert2("식별번호  ", 8), convert(" : "+rCardNumber, 30));
                            printer.printString(cardNumber).feedLine();

                            String approvalMoney = String.format("%s%s",
                                    convert2("승인금액  ", 8),
                                    convert(" : "+formatPrice(Integer.parseInt(rApprovalMoney)), 30));
                            printer.printString(approvalMoney).feedLine();

                            String approvalNumber = String.format("%s%s",
                                    convert2("승인번호  ", 8),
                                    convert(" : "+rApprovalNumber, 30));
                            printer.printString(approvalNumber).feedLine();

                            String approvalDate = String.format("%s%s",
                                    convert2("승인일시  ", 8),
                                    convert(" : "+rApprovalDate, 30));
                            printer.printString(approvalDate).feedLine();
                        }
                        // 현금
                        else if(rIssuingCompany.equals("cash")) {

                        }
                        // 신용걸래
                        else {
                            printer.printText("<<< 신 용 승 인 정 보 >>>\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_1HEIGHT);
                            String cardCompanyName = String.format("%s%s",
                                    convert2("카드종류  ", 8),
                                    convert(" : "+rIssuingCompany, 30));
                            printer.printString(cardCompanyName).feedLine();

                            String cardNumber = String.format("%s%s",
                                    convert2("카드번호  ", 8),
                                    convert(" : "+rCardNumber, 30));
                            printer.printString(cardNumber).feedLine();

                            String installment = String.format("%s%s",
                                    convert2("할부개월  " , 8),
                                    convert(" : "+"일시불  ", 30));
                            printer.printString(installment).feedLine();

                            String approvalMoney = String.format("%s%s",
                                    convert2("승인금액  ", 8),
                                    convert(" : "+formatPrice(Integer.parseInt(rApprovalMoney)), 30));
                            printer.printString(approvalMoney).feedLine();

                            String approvalNumber = String.format("%s%s",
                                    convert2("승인번호  ", 8),
                                    convert(" : "+rApprovalNumber, 30));
                            printer.printString(approvalNumber).feedLine();

                            String approvalDate = String.format("%s%s",
                                    convert2("승인일시  ", 8),
                                    convert(" : "+rApprovalDate, 30));
                            printer.printString(approvalDate).feedLine();
                        }
                    }
                    // 카드 정보 세팅 end

                    // 싸인 세팅
                    String filePath = outSignFilePath;
                    if(filePath != null) {
                        // File 객체를 생성합니다.
                        File file = new File(filePath);
                        // 파일이 존재하는지 확인합니다.
                        if (file.exists()) {
                            printer.printString("-----------------------------------------------").feedLine();
                            printer.printString("(서명)").feedLine(1);
                            printer.initializePrinter().printBitmap(outSignFilePath, POSConst.ALIGNMENT_CENTER, 300);
                            printer.feedLine(2);
                            outSignFilePath = "";
                            receiptImgDelete();
                        }
                    }
                    // 싸인 세팅 end
                    printer.printString("-----------------------------------------------").feedLine(5).cutPaper();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        Handler receiptHandler = new Handler();
        receiptHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isStartingReceipt = false;
            }
        }, 500);
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

    // 메뉴 이름 길면 ... 처리
    private String shortenString(String input, int maxLength) {
        if (input.length() > maxLength) {
            return input.substring(0, maxLength-1) + "...";
        } else {
            return input;
        }
    }

    private void receiptImgDelete() {
        // Get the directory path
        String directoryPath = "/storage/emulated/0/KISMOBILE/";

        // Create a File object with the directory path
        File directory = new File(directoryPath);

        // Check if the directory exists and is actually a directory
        if (directory.exists() && directory.isDirectory()) {
            // Get a list of files inside the directory
            File[] files = directory.listFiles();

            // Check if the files array is not null and contains any files
            if (files != null && files.length > 0) {
                // Loop through each file and delete them
                for (File file : files) {
                    // Attempt to delete the file
                    if (file.delete()) {
                        // File deleted successfully
                        // You can perform any additional actions here after each file is deleted
                        // For example, log the deletion or update your app's UI
                    } else {
                        // File deletion failed
                        // You can handle this case (e.g., show an error message) if necessary
                    }
                }
            } else {
                // No files found inside the directory
                // You can handle this case (e.g., show a message indicating no files found)
            }
        } else {
            // Directory does not exist or is not a directory
            // You can handle this case (e.g., show an error message) if necessary
        }
    }

    public void printClose() {
        connect.close();
        isStartingReceipt = false;
    }
}
