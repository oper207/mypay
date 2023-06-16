package com.softrain.mypay;

import android.content.Context;
import android.os.AsyncTask;

public class HTTPBaseClass extends AsyncTask<String,Void,Integer> {
   Context mContext;
   String RetResult;
   String RetHttpStatusCode;
   String RetHttpBody;

   public HTTPBaseClass(Context context) {
      mContext = context;
   }

   @Override
   protected Integer doInBackground(String... params) {
      if (ConstFunc.isNetConnect(mContext)) {
         if (params[4].equals(ConstDef.HTTP_TYPE_DATA)) {
            String[] Ret = ConstFunc.httpRequestData(mContext, params[1], params[2], params[3]);
            RetResult = Ret[0];
            RetHttpStatusCode = Ret[1];
            RetHttpBody = Ret[2];
         }
         else if (params[4].equals(ConstDef.HTTP_TYPE_FILE)) {
            String[] Ret = ConstFunc.httpRequestFile(mContext, params[1], params[2], params[3]);
            RetResult = Ret[0];
            RetHttpStatusCode = Ret[1];
            RetHttpBody = Ret[2];
         }
         else {
            RetResult = ConstDef.RETURN_RESULT_VALUE_NETWORK_DISCONNECTED;
            RetHttpStatusCode = "0";
            RetHttpBody = "";
         }
      }
      else {
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
