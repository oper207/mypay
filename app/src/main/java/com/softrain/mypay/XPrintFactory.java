//package com.softrain.mypay;
//
//import android.content.Context;
//import android.os.Environment;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.blankj.utilcode.util.CrashUtils;
//import com.softrain.mypay.XprinterUtils.HexUtils;
//import com.softrain.mypay.XprinterUtils.SpUtils;
//import com.szsicod.print.escpos.PrinterAPI;
//import com.szsicod.print.log.AndroidLogCatStrategy;
//import com.szsicod.print.log.Logger;
//import com.szsicod.print.log.Utils;
//
//import net.posprinter.IDeviceConnection;
//import net.posprinter.POSConnect;
//import net.posprinter.POSConst;
//import net.posprinter.POSPrinter;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.File;
//import java.text.DecimalFormat;
//
//public class XPrintFactory {
//
//    // POS 프린터 연결을 위한 IDeviceConnection 객체
//    private IDeviceConnection connect;
//
//    // POS 프린터 객체
//    private POSPrinter printer;
//
//    public void xInit(Context context) {
//        connect("MAC");
//    }
//
//    // 프린터 연결을 수행하는 메서드
//    private void connect(String mac) {
//        Log.e("프린트 값"," : "+ connect);
//        if (connect != null) {
//            connect.close();
//        }
//        connect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_USB);
//        connect.connect(null, (code, msg) -> {
//            if (code == POSConnect.CONNECT_SUCCESS) {
//                // 연결 성공 시
//                printer = new POSPrinter(connect);
//                connect.startReadLoop(this::receiveData);
//            } else if (code == POSConnect.CONNECT_FAIL) {
//                // 연결 실패 시
//            } else if (code == POSConnect.CONNECT_INTERRUPT) {
//                // 연결 중단 시
//            }
//        });
//    }
//    // 데이터 수신 처리 메서드
//    private void receiveData(byte[] data) {
//        String hexString = HexUtils.bytes2HexStrWithSpace(data, data.length);
//        // 사용할 코드로 처리하세요
//        // (HexUtils.bytes2String(data))
//    }
//    private void XprintReceipt(String rReceiptPageNum, String rStore, String rReceipt, String rCard, String rBill) {
//        if(printer != null) {
//            try {
//                // 매장정보
//                JSONObject joStore = new JSONObject(rStore);
//                String sName = joStore.getString("sName");
//                String sAddress = joStore.getString("sAddress");
//                String sRepresentative = joStore.getString("sRepresentative");
//                String sTel = joStore.getString("sTel");
//                String sOrderDate = joStore.getString("sOrderDate");
//                String sOrderNumber = joStore.optString("sOrderNumber", null);
//
//                printer.setCharSet("CP949");
//                // 메뉴 교환권이 필요 시
//                if(rReceiptPageNum.equals("2")){
//                    printer.printText("[메뉴 교환권]\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, 2).feedLine();
//                    String couponNumber = String.format("%s%s",
//                            convert("교환권 번호", 13),
//                            convert(": "+"0157", 1));
//                    printer.printString(couponNumber).feedLine();
//                    String couponDate = String.format("%s%s",
//                            convert("일시", 13),
//                            convert(": "+sOrderDate, 1));
//                    printer.printString(couponDate).feedLine();
//                    printer.printString("-----------------------------------------------").feedLine();
//                    String couponCategories = String.format("%s%s",
//                            convert("상품명", 40),
//                            convert("수량", 5));
//                    printer.printString(couponCategories).feedLine();
//                    printer.printString("-----------------------------------------------").feedLine();
//                    JSONArray jaCouponData = new JSONArray(rReceipt);
//                    for (int i = 0; i < jaCouponData.length(); i++) {
//                        JSONObject joCouponData = jaCouponData.getJSONObject(i);
//                        // JSON 객체에서 필요한 값을 추출
//                        String name = joCouponData.getString("name");
//                        int quantity = joCouponData.getInt("quantity");
//                        StringBuilder sbCoupon = new StringBuilder();
//                        sbCoupon.append(convert(name, 42));
//                        sbCoupon.append(convert(String.valueOf(quantity), 5));
//                        printer.printString(sbCoupon.toString()).feedLine();
//                    }
//                    printer.printString("-----------------------------------------------").feedLine(5);
//                    printer.cutPaper();
//                }
//
//                printer.initializePrinter().printText("[영수증]\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, 2).feedLine();
//                String storeName = String.format("%s%s",
//                        convert("매 장 명", 10),
//                        convert(sName, 30));
//                printer.printString(storeName).feedLine();
//
//                String storeAddress = String.format("%s%s",
//                        convert("매장주소", 10),
//                        convert(sAddress, 30));
//                printer.printString(storeAddress).feedLine();
//
//                String storeRepresentative = String.format("%s%s",
//                        convert("대표자", 10),
//                        convert(sRepresentative, 30));
//                printer.printString(storeRepresentative).feedLine();
//
//                String storeRepresentativeNumber = String.format("%s%s",
//                        convert("TEL", 10),
//                        convert(sTel, 30));
//                printer.printString(storeRepresentativeNumber).feedLine();
//
//                String storeOrderDate = String.format("%s%s",
//                        convert("주문일시", 10),
//                        convert(sOrderDate, 30));
//                printer.printString(storeOrderDate).feedLine();
//
//                String storeOrderNumber = String.format("%s%s",
//                        convert("룸번호", 10),
//                        convert(sOrderNumber, 30));
//                printer.printString(storeOrderNumber).feedLine();
//                printer.printString("-----------------------------------------------").feedLine();
//
//                String storeCategories = String.format("%s%s%s%s",
//                        convert("상품명", 23),
//                        convert("단가", 8),
//                        convert("수량", 8),
//                        convert("금액", 9));
//                printer.printTextAttribute(storeCategories, POSConst.FNT_BOLD).printString("-----------------------------------------------").feedLine();
//
//                JSONArray jaStoreData = new JSONArray(rReceipt);
//                for (int i = 0; i < jaStoreData.length(); i++) {
//                    JSONObject joStoreData = jaStoreData.getJSONObject(i);
//                    // JSON 객체에서 필요한 값을 추출
//                    String name = joStoreData.getString("name");
//                    String price = joStoreData.getString("price");
//                    int quantity = joStoreData.getInt("quantity");
//                    int amount = joStoreData.getInt("quantity") * joStoreData.getInt("price");
//                    StringBuilder sbStoreData = new StringBuilder();
//                    sbStoreData.append(convert(name, 22));
//                    sbStoreData.append(convert(formatPrice(Integer.parseInt(price)), 10));
//                    sbStoreData.append(convert(String.valueOf(quantity), 7));
//                    sbStoreData.append(convert(formatPrice(amount), 9));
//                    printer.printString(sbStoreData.toString());
//                }
//
//                printer.printString("-----------------------------------------------").feedLine();
//
//                // 돈 세금
//                JSONObject joBill = new JSONObject(rBill);
//                String sTotal = joBill.getString("total");
//                String sTax = joBill.getString("tax");
//                String storeMoney = String.format("%s%s",
//                        convert("공급가액", 39),
//                        convert(formatPrice(Integer.parseInt(sTotal)-Integer.parseInt(sTax)), 9));
//                printer.printString(storeMoney);
//
//                String storeSurtax = String.format("%s%s",
//                        convert("부 가 세", 39),
//                        convert(formatPrice(Integer.parseInt(sTax)), 9));
//                printer.printString(storeSurtax).printString("-----------------------------------------------").feedLine();
//
//                String storeResult = String.format("%s%s",
//                        convert("합계", 39),
//                        convert(formatPrice(Integer.parseInt(sTotal)), 9));
//                printer.printString(storeResult).printString("-----------------------------------------------").feedLine();
//
//                JSONArray jaCardInfo = new JSONArray(rCard);
//                for (int i = 0; i < jaCardInfo.length(); i++) {
//                    JSONObject joCardInfo = jaCardInfo.getJSONObject(i);
//                    // JSON 객체에서 필요한 값을 추출
//                    String rIssuingCompany = joCardInfo.getString("issuingCompany");
//                    String rCardNumber = joCardInfo.getString("cardNumber");
//                    String rApprovalNumber = joCardInfo.getString("approvalNumber");
//                    String rApprovalDate = joCardInfo.getString("approvalDate");
//                    String rMerchantNumber = joCardInfo.getString("merchantNumber");
//                    String rApprovalMoney = joCardInfo.getString("approvalMoney");
//                    if(rIssuingCompany.equals("cashReceipt")) {
//                        printer.printText("<<< 현금영수증 >>>\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_1HEIGHT);
//
//                        String cardNumber = String.format("%s%s",
//                                convert2("식별번호  ", 8),
//                                convert(" : "+rCardNumber, 30));
//                        printer.printString(cardNumber).feedLine();
//
//                        int total = Integer.parseInt(rApprovalMoney);
//                        String approvalMoney = String.format("%s%s",
//                                convert2("승인금액  ", 8),
//                                convert(" : "+formatPrice(Integer.parseInt(rApprovalMoney)), 30));
//                        printer.printString(approvalMoney).feedLine();
//
//                        String approvalNumber = String.format("%s%s",
//                                convert2("승인번호  ", 8),
//                                convert(" : "+rApprovalNumber, 30));
//                        printer.printString(approvalNumber).feedLine();
//
//                        String approvalDate = String.format("%s%s",
//                                convert2("승인일시  ", 8),
//                                convert(" : "+rApprovalDate, 30));
//                        printer.printString(approvalDate).feedLine();
//                    }
//                    else if(rIssuingCompany.equals("cash")) {
//
//                    }
//                    // 신용걸래
//                    else {
//                        printer.printText("<<< 신 용 승 인 정 보 >>>\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_1HEIGHT);
//                        String cardCompanyName = String.format("%s%s",
//                                convert2("카드종류  ", 8),
//                                convert(" : "+rIssuingCompany, 30));
//                        printer.printString(cardCompanyName).feedLine();
//
//                        String cardNumber = String.format("%s%s",
//                                convert2("카드번호  ", 8),
//                                convert(" : "+rCardNumber, 30));
//                        printer.printString(cardNumber).feedLine();
//
//                        String installment = String.format("%s%s",
//                                convert2("할부개월  " , 8),
//                                convert(" : "+"일시불  ", 30));
//                        printer.printString(installment).feedLine();
//
//                        int total = Integer.parseInt(rApprovalMoney);
//                        String approvalMoney = String.format("%s%s",
//                                convert2("승인금액  ", 8),
//                                convert(" : "+formatPrice(Integer.parseInt(rApprovalMoney)), 30));
//                        printer.printString(approvalMoney).feedLine();
//
//                        String approvalNumber = String.format("%s%s",
//                                convert2("승인번호  ", 8),
//                                convert(" : "+rApprovalNumber, 30));
//                        printer.printString(approvalNumber).feedLine();
//
//                        String approvalDate = String.format("%s%s",
//                                convert2("승인일시  ", 8),
//                                convert(" : "+rApprovalDate, 30));
//                        printer.printString(approvalDate).feedLine();
//
//                        String purchaseCompany = String.format("%s%s",
//                                convert2("가맹점번호", 8),
//                                convert(" : "+rMerchantNumber, 30));
//                        printer.printString(purchaseCompany).feedLine();
//                    }
//
//                }
//                // 파일 경로를 문자열로 저장합니다.
//                String filePath = kisvanSpec.outSignFilePath;
//                if(filePath != null) {
//                    // File 객체를 생성합니다.
//                    File file = new File(filePath);
//                    // 파일이 존재하는지 확인합니다.
//                    if (file.exists()) {
//                        printer.printString("-----------------------------------------------").feedLine();
//                        printer.printString("(서명)").feedLine(1);
//                        printer.initializePrinter().printBitmap(filePath, POSConst.ALIGNMENT_CENTER, 300);
//                        printer.feedLine(2);
//                        kisvanSpec.outSignFilePath = "";
//                    }
//                }
//                printer.printString("-----------------------------------------------").feedLine(5).cutPaper();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    // 한국 돈 표기법으로 형식화된 문자열 반환
//    public static String formatPrice(int price) {
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
//        return decimalFormat.format(price);
//    }
//
//    // 전각문자의 개수만큼 문자열 길이를 빼주는 메서드
//    public static String convert(String word, int size) {
//        String formatter = String.format("%%-%ds", size - getKorCnt(word));
//        return String.format(formatter, word);
//    }
//
//    // 전각문자의 개수만큼 문자열 길이를 빼주는 메서드
//    public static String convert2(String word, int size) {
//        String formatter = String.format("%%%ds", size - getKorCnt(word));
//        return String.format(formatter, word);
//    }
//
//    // 전각문자 개수를 세주는 메서드
//    private static int getKorCnt(String kor) {
//        int cnt = 0;
//        for (int i = 0; i < kor.length(); i++) {
//            if (kor.charAt(i) >= '가' && kor.charAt(i) <= '힣') {
//                cnt++;
//            }
//        }
//        return cnt;
//    }
//}
