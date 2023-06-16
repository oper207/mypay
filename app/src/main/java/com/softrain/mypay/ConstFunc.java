package com.softrain.mypay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ConstFunc {
   public static boolean isNetConnect(Context context) {
      try {
         ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
         if (netInfo != null && netInfo.isConnected()) {
            return true;
         }
      }
      catch (Exception e) {
         TextLog.o("[error] Not Connected");
      }

      return false;
   }

   public static String getAppVersionName(Context context) {
      PackageInfo packageInfo;

      try {
         packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      }
      catch (PackageManager.NameNotFoundException e) {
         return "unknown";
      }

      return packageInfo.versionName;
   }

   public static int getAppVersionCode(Context context) {
      PackageInfo packageInfo;
      int versionCode;

      try {
         packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            versionCode = (int)packageInfo.getLongVersionCode();
         }
         else {
            versionCode = packageInfo.versionCode;
         }
      }
      catch (PackageManager.NameNotFoundException e) {
         return 0;
      }

      return versionCode;
   }

   public static String[] httpRequestData(Context context, String a_method, String a_url, String a_body) {
      String ret[] = new String[3];
      int http_response_code = 0;
      String http_response_body = new String();

      TextLog.o("[Req] HttpMethod = " + a_method);
      TextLog.o("[Req] HttpUrl = " + a_url);
      TextLog.o("[Req] HttpBody = " + a_body);

      try {
         URL url = new URL(a_url);
         HttpURLConnection conn = null;
         if (url.getProtocol().toLowerCase().equals("https")) {
            trust_all_hosts();
            HttpsURLConnection https = (HttpsURLConnection)url.openConnection();
            https.setHostnameVerifier(new HostnameVerifier() {
               @Override
               public boolean verify(String hostname, SSLSession session) {
                  return true;
               }
            });
            conn = https;
         }
         else {
            conn = (HttpURLConnection)url.openConnection();
         }

         if (conn != null) {
            conn.setReadTimeout(3000); /* 10000 -> 3000 */
            conn.setConnectTimeout(3000); /* 10000 -> 3000 */
            conn.setDoInput(true);

            conn.setRequestMethod(a_method);
            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("User-Agent", "mypay/1.0");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Accept-Language", "ko-kr");
            conn.setRequestProperty("Accept-Encoding", "");

            if (a_method.equals("GET")) {
               conn.setDoOutput(false);
            }
            else { /* POST */
               conn.setRequestProperty("Content-Type", "application/json");
               if (a_body.length() > 0) { /* POST: a_body is json text */
                  conn.setDoOutput(true);
                  conn.setUseCaches(false);

                  DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

                  wr.writeBytes(a_body);

                  wr.flush();
                  wr.close();
               }
            }

            http_response_code = conn.getResponseCode();

            if (http_response_code == HttpURLConnection.HTTP_OK || http_response_code == HttpURLConnection.HTTP_CREATED || http_response_code == HttpURLConnection.HTTP_ACCEPTED) {
               if (a_method.equals("GET")) {
                  int i, idx = 0, nread = 0;
                  int content_length = conn.getContentLength();
                  byte[] tmp = new byte[content_length + 1];
                  byte[] content = new byte[content_length + 1];

                  InputStream is = conn.getInputStream();

                  for (;;) {
                     nread = is.read(tmp);
                     if (nread <= 0) { break; }
                     for (i = 0; i < nread; i++, idx++) {
                        content[idx] = tmp[i];
                     }
                  }

                  is.close();

                  http_response_body = new String(content, 0, idx, StandardCharsets.UTF_8);
               }
               else { /* POST */
                  int i, idx = 0, nread = 0;
                  int content_length = conn.getContentLength();
                  byte[] tmp = new byte[content_length + 1];
                  byte[] content = new byte[content_length + 1];

                  InputStream is = conn.getInputStream();

                  for (;;) {
                     nread = is.read(tmp);
                     if (nread <= 0) { break; }
                     for (i = 0; i < nread; i++, idx++) {
                        content[idx] = tmp[i];
                     }
                  }

                  is.close();

                  http_response_body = new String(content, 0, idx, StandardCharsets.UTF_8);
               }

               conn.disconnect();

               ret[0] = ConstDef.RETURN_RESULT_VALUE_OK;
               ret[1] = Integer.toString(http_response_code);
               ret[2] = http_response_body;
            }
            else {
               TextLog.o("[error] httpRequest: Server Error Response [1] = " + http_response_code);

               if (conn != null) { conn.disconnect(); }

               ret[0] = ConstDef.RETURN_RESULT_VALUE_SERVER_ERROR_RESPONSE;
               ret[1] = Integer.toString(http_response_code);
               ret[2] = a_url;
            }
         }
         else {
            TextLog.o("[error] httpRequest: Server Error Response [2] = " + http_response_code);

            if (conn != null) { conn.disconnect(); }

            ret[0] = ConstDef.RETURN_RESULT_VALUE_SERVER_ERROR_RESPONSE;
            ret[1] = Integer.toString(http_response_code);
            ret[2] = a_url;
         }
      }
      catch (Exception e) {
         TextLog.o("[error] httpRequest: Server Timeout");

         ret[0] = ConstDef.RETURN_RESULT_VALUE_SERVER_TIMEOUT;
         ret[1] = Integer.toString(http_response_code);
         ret[2] = a_url;
      }

      return ret;
   }

   public static String[] httpRequestFile(Context context, String a_method, String a_url, String a_body) {
      String ret[] = new String[3];
      int http_response_code = 0;
      String http_response_body = new String();

      TextLog.o("[Req] HttpMethod = " + a_method);
      TextLog.o("[Req] HttpUrl = " + a_url);
      TextLog.o("[Req] HttpBody = " + a_body);

      try {
         URL url = new URL(a_url);
         HttpURLConnection conn = null;
         if (url.getProtocol().toLowerCase().equals("https")) {
            trust_all_hosts();
            HttpsURLConnection https = (HttpsURLConnection)url.openConnection();
            https.setHostnameVerifier(new HostnameVerifier() {
               @Override
               public boolean verify(String hostname, SSLSession session) {
                  return true;
               }
            });
            conn = https;
         }
         else {
            conn = (HttpURLConnection)url.openConnection();
         }

         if (conn != null) {
            conn.setReadTimeout(20000); /* 10000 -> 20000 */
            conn.setConnectTimeout(20000); /* 10000 -> 20000 */
            conn.setDoInput(true);

            conn.setRequestMethod(a_method);
            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("User-Agent", "mypay/1.0");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Accept-Language", "ko-kr");
            conn.setRequestProperty("Accept-Encoding", "");

            if (a_method.equals("GET")) {
               conn.setDoOutput(false);
            }
            else { /* POST */
               conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=espring");
               if (a_body.length() > 0) { /* POST: a_body is upload filename */
                  InputStream input = null;

                  conn.setDoOutput(true);
                  conn.setUseCaches(false);

                  DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                  wr.writeBytes("--" + "espring" + "\r\n");
                  wr.writeBytes("Content-Disposition: form-data; name=\"files\"; filename=\"" + a_body.substring(a_body.lastIndexOf("/") + 1) + "\"\r\n");
                  wr.writeBytes("\r\n");

                  try {
                     input = new FileInputStream(a_body);
                     int bytesAvailable = input.available();
                     int maxBufferSize = 4096;
                     int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                     byte[] buffer = new byte[bufferSize];

                     int bytesRead = input.read(buffer, 0, bufferSize);
                     while (bytesRead > 0) {
                        wr.write(buffer, 0, bufferSize);
                        bytesAvailable = input.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = input.read(buffer, 0, bufferSize);
                     }
                  }
                  catch (FileNotFoundException e) {
                     TextLog.o("[error] httpRequest: File not found: " + a_body);
                  }
                  finally {
                     if (input != null) {
                        input.close();
                     }
                  }

                  wr.writeBytes("\r\n");
                  wr.writeBytes("--" + "espring" + "--\r\n");

                  wr.flush();
                  wr.close();
               }
            }

            http_response_code = conn.getResponseCode();

            if (http_response_code == HttpURLConnection.HTTP_OK || http_response_code == HttpURLConnection.HTTP_CREATED || http_response_code == HttpURLConnection.HTTP_ACCEPTED) {
               if (a_method.equals("GET")) {
                  int nread = 0;
                  int content_length = conn.getContentLength();
                  byte[] tmp = new byte[content_length + 1];

                  InputStream is = conn.getInputStream();
                  String download_file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + ConstDef.DOWNLOAD_DIR + "/" + a_url.substring(a_url.lastIndexOf('/') + 1) /* context.getExternalFilesDir(null).getAbsolutePath() + "/" + a_url.substring(a_url.lastIndexOf('/') + 1) */;
                  TextLog.o("httpRequestFile: download_file = " + download_file);
                  File file = new File(download_file);
                  FileOutputStream fos = new FileOutputStream(file);
                  for (;;) {
                     nread = is.read(tmp);
                     if (nread <= 0) { break; }
                     fos.write(tmp, 0, nread);
                  }
                  is.close();
                  fos.close();

                  http_response_body = download_file; /* full path */
               }
               else { /* POST */
                  int i, idx = 0, nread = 0;
                  int content_length = conn.getContentLength();
                  byte[] tmp = new byte[content_length + 1];
                  byte[] content = new byte[content_length + 1];

                  InputStream is = conn.getInputStream();

                  for (;;) {
                     nread = is.read(tmp);
                     if (nread <= 0) { break; }
                     for (i = 0; i < nread; i++, idx++) {
                        content[idx] = tmp[i];
                     }
                  }

                  is.close();

                  http_response_body = new String(content, 0, idx, StandardCharsets.UTF_8);
               }
               // else { /* POST */
               //   http_response_body = a_body; /* full path */
               // }

               conn.disconnect();

               ret[0] = ConstDef.RETURN_RESULT_VALUE_OK;
               ret[1] = Integer.toString(http_response_code);
               ret[2] = http_response_body;
            }
            else {
               TextLog.o("[error] httpRequest: Server Error Response [1] = " + http_response_code);

               if (conn != null) { conn.disconnect(); }

               ret[0] = ConstDef.RETURN_RESULT_VALUE_SERVER_ERROR_RESPONSE;
               ret[1] = Integer.toString(http_response_code);
               ret[2] = a_url;
            }
         }
         else {
            TextLog.o("[error] httpRequest: Server Error Response [2] = " + http_response_code);

            if (conn != null) { conn.disconnect(); }

            ret[0] = ConstDef.RETURN_RESULT_VALUE_SERVER_ERROR_RESPONSE;
            ret[1] = Integer.toString(http_response_code);
            ret[2] = a_url;
         }
      }
      catch (Exception e) {
         TextLog.o("[error] httpRequest: Server Timeout");

         ret[0] = ConstDef.RETURN_RESULT_VALUE_SERVER_TIMEOUT;
         ret[1] = Integer.toString(http_response_code);
         ret[2] = a_url;
      }

      return ret;
   }

   public static void trust_all_hosts() {
      /* Create a trust manager that does not validate certificate chains */
      TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
         public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
         }

         @Override
         public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
            /* TODO Auto-generated method stub */
         }

         @Override
         public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
            /* TODO Auto-generated method stub */
         }
      }};

      /* Install the all-trusting trust manager */
      try {
         SSLContext sc = SSLContext.getInstance("TLS");
         sc.init(null, trustAllCerts, new java.security.SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void installApk(Context context, String filePath) {
      TextLog.o("filePath = " + filePath);
      TextLog.o("context.getPackageName() = " + context.getPackageName());

      File toInstall = new File(filePath);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
         Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", toInstall);
         Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
         intent.setData(apkUri);
         intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
         context.startActivity(intent);
      } else {
         Uri apkUri = Uri.fromFile(toInstall);
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         context.startActivity(intent);
      }
   }
}
