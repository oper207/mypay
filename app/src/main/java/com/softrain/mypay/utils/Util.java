package com.softrain.mypay.utils;

// 문자열의 왼쪽에 지정한 길이만큼 특정 문자(fillChar)를 채워넣어 반환하는 메소드
// 예를 들어, Util.leftPad("123", "0", 5)를 호출하면 "00123"이 반환
public class Util {
    public static String leftPad(String str, String fillChar, int length)
    {
        // 길이가 1이 아니면 빈 문자열을 반환
        if (fillChar.length() != 1) {
            return "";
        }

        // str의 길이가 length보다 크면 str 자체를 반환
        if (str.length() > length)
            return str;

        String returnStr = "";
        int i;
        // str의 길이가 length보다 작으면 fillChar 문자로 length 길이까지 채워진다.
        for (i = str.length(); i < length; i++) {
            returnStr = returnStr + fillChar;
        }

        returnStr = returnStr + str;
        // 완성된 문자열을 반환
        return returnStr;
    }
}
