package com.softrain.mypay.XprinterUtils

import android.content.Context

object SpUtils {
    // 상수로 정의된 문자열 상수
    const val LAST_MAC = "LAST_MAC"
    // 기본 파일 이름으로 사용되는 변수
    var defFileName = "printerp1"

    // SharedPreferences 인스턴스를 반환하는 비공개 함수
    private fun getShare() = App.get().getSharedPreferences(defFileName, Context.MODE_PRIVATE)

    // key에 해당하는 문자열 값을 SharedPreferences에서 가져옵니다. 값이 없을 경우 defValue를 반환
    fun getString(key:String, defValue: String): String{
        return getShare().getString(key, defValue)?:defValue
    }

    // key와 value를 SharedPreferences에 저장
    fun putString(key: String, value: String){
        getShare().edit().putString(key,value).apply()
    }
    // key에 해당하는 부울 값을 SharedPreferences에서 가져옵니다. 값이 없을 경우 defValue를 반환
    fun getBoolean(key:String, defValue: Boolean): Boolean{
        return getShare().getBoolean(key, defValue)
    }
    // key와 value를 SharedPreferences에 저장
    fun putBoolean(key:String, value: Boolean){
        getShare().edit().putBoolean(key, value).apply()
    }

}