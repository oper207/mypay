package com.softrain.mypay.XprinterUtils;

import java.io.UnsupportedEncodingException;

public class HexUtils {

    // 바이트 배열을 16진수 문자열로 변환하는 메소드: 주어진 바이트 배열 src의 크기를 size만큼 반복하면서 각 바이트를 16진수 문자열로 반환하도 이어 붙여서 반환한다.
    public static String bytesToHexString(byte[] src, int size) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return "";
        }
        for (int i = 0; i < size; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    // 바이트 배열을 16진수 문자열로 변환하는 메서드
    public static String bytes2HexStrWithSpace(byte[] src, int size) {
        String ret = bytesToHexString(src, size); // 메서드를 호출하여 바이트 배열을 16진수 문자열로 변환한 후, 각 바이트 값 사이에 공백을 추가하여 반환
        String regex = "(.{2})";
        return ret.replaceAll(regex, "$1 ");
    }

    // 16진수 문자열을 바이트 배열로 변환하는 메서드
    public static byte[] hexStringToBytes(String hexString) {
        // 주어진 16진수 문자열 hexString을 받아 각 16진수 문자 쌍을 바이트로 변환하여 반환
        if (hexString == null || hexString.equals("")) {
            return new byte[0];
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    // 문자를 바이트로 변환하는 메서드입니다. 주어진 문자 c가 "0123456789ABCDEF" 문자열 내에서 몇 번째 인덱스인지 찾아서 해당 인덱스 값을 바이트로 반환
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] strTobytes(String str){
        byte[] b=null,data=null;
        try {
            b = str.getBytes("utf-8");
            data=new String(b,"utf-8").getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;
    }

    // 바이트 배열을 문자열로 변환하는 메서드입니다. 주어진 바이트 배열 bytes를 "gbk" 인코딩을 사용하여 문자열로 변환
    public static String bytes2String(byte[] bytes){//gbk的bytes
        if (bytes == null)
            return "";

        String ret = "";
        try {
            ret = new String(bytes, "gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    // str에서 각 두 개의 문자(바이트) 사이에 공백을 추가하는 메서드
    public static String addHexSpace(String str){
        String regex = "(.{2})"; // 정규식 (.{2})를 사용하여 문자열을 두 글자씩 나누고, 각 나누어진 부분 문자열에 공백을 추가하여 새로운 문자열을 생성
        return str.replaceAll(regex, "$1 ").trim();
    }

}
