package com.softrain.mypay;

import android.content.Context;
import android.os.AsyncTask;

// HTTP 요청을 처리하는 기반 클래스로 AsyncTask를 상속하여 비동기 처리
public class HTTPBaseClass extends AsyncTask<String,Void,Integer> {
   // Context 객체와 결과 데이터 관련 변수 선언
   Context mContext;
   String RetResult; // HTTP 요청 결과 (OK 등)
   String RetHttpStatusCode; // HTTP 상태 코드 (200, 404 등)
   String RetHttpBody; // HTTP 응답 본문 데이터

   // 생성자: Context를 받아옴 (HTTP 요청을 위해 필요한 Context)
   public HTTPBaseClass(Context context) {
      mContext = context;
   }

   // 비동기 작업이 실행되는 메서드
   @Override
   protected Integer doInBackground(String... params) {
      // 네트워크 연결 확인
      if (ConstFunc.isNetConnect(mContext)) {
         if (params[4].equals(ConstDef.HTTP_TYPE_DATA)) {
            // 데이터 전송을 위한 HTTP 요청 실행
            String[] Ret = ConstFunc.httpRequestData(mContext, params[1], params[2], params[3]);
            RetResult = Ret[0];
            RetHttpStatusCode = Ret[1];
            RetHttpBody = Ret[2];
         }
         else if (params[4].equals(ConstDef.HTTP_TYPE_FILE)) {
            // 파일 전송을 위한 HTTP 요청 실행
            String[] Ret = ConstFunc.httpRequestFile(mContext, params[1], params[2], params[3]);
            RetResult = Ret[0];
            RetHttpStatusCode = Ret[1];
            RetHttpBody = Ret[2];
         }
         else {
            // 지원하지 않는 HTTP 요청 타입인 경우
            RetResult = ConstDef.RETURN_RESULT_VALUE_NETWORK_DISCONNECTED;
            RetHttpStatusCode = "0";
            RetHttpBody = "";
         }
      }
      else {
         // 네트워크가 연결되어 있지 않은 경우
         RetResult = ConstDef.RETURN_RESULT_VALUE_NETWORK_DISCONNECTED;
         RetHttpStatusCode = "0";
         RetHttpBody = "";
      }

      TextLog.o("[Resp] Result = " + RetResult);
      TextLog.o("[Resp] HttpStatusCode = " + RetHttpStatusCode);
      TextLog.o("[Resp] HttpBody = " + RetHttpBody);

      return Integer.valueOf(params[0]);
   }
}
