package com.imageshare.upload;
/**
 * Created by jaygo on 14-4-20.
 */
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class UploadUtil {
    private static UploadUtil uploadUtil;
    private static final String BOUNDARY = UUID.randomUUID().toString();
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private static final String CONTENT_TYPE = "multipart/form-data";

    private UploadUtil() {
    }

    public static UploadUtil getInstance() {
        if (null == uploadUtil) {
            uploadUtil = new UploadUtil();
        }
        return uploadUtil;
    }

    private static final String TAG = "UploadUtil";
    private int readTimeOut = 10 * 1000;
    private int connectTimeout = 10 * 1000;
    private static int requestTime = 0;
    private static final String CHARSET = "utf-8";
    public static final int UPLOAD_SUCCESS_CODE = 1;
    public static final int UPLOAD_FILE_NOT_EXISTS_CODE = 2;
    public static final int UPLOAD_SERVER_ERROR_CODE = 3;

    public void uploadFile(String filePath, String fileKey, String RequestURL,
                           Map<String, String> param) {
        if (filePath == null) {
            sendMessage(UPLOAD_FILE_NOT_EXISTS_CODE, "要上传的文件不存在！");
            return;
        }
        try {
            File file = new File(filePath);
            uploadFile(file, fileKey, RequestURL, param);
        } catch (Exception e) {
            sendMessage(UPLOAD_FILE_NOT_EXISTS_CODE, "要上传的文件不存在！（异常）");
            e.printStackTrace();
            return;
        }
    }

    public void uploadFile(final File file, final String fileKey,
                           final String RequestURL, final Map<String, String> param) {
        if (file == null || (!file.exists())) {
            sendMessage(UPLOAD_FILE_NOT_EXISTS_CODE, "要上传的文件不存在！");
            return;
        }

        Log.i(TAG, "URL=" + RequestURL);
        Log.i(TAG, "fileName=" + file.getName());
        Log.i(TAG, "fileKey=" + fileKey);
        new Thread(new Runnable() {
            public void run() {
                toUploadFile(file, fileKey, RequestURL, param);
            }
        }).start();
    }

    private void toUploadFile(File file, String fileKey, String RequestURL,
                              Map<String, String> param) {
        String result = null;
        requestTime = 0;

        long requestTime = System.currentTimeMillis();
        long responseTime = 0;

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(readTimeOut);
            conn.setConnectTimeout(connectTimeout);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", CHARSET);
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            StringBuffer sb = null;
            String params = "";

            if (param != null && param.size() > 0) {
                Iterator<String> it = param.keySet().iterator();
                while (it.hasNext()) {
                    sb = null;
                    sb = new StringBuffer();
                    String key = it.next();
                    String value = param.get(key);
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_END).append(LINE_END);
                    sb.append(value).append(LINE_END);
                    params = sb.toString();
                    Log.i(TAG, key + "=" + params + "##");
                    dos.write(params.getBytes());
                }
            }

            sb = null;
            params = null;
            sb = new StringBuffer();

            sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
            sb.append("Content-Disposition:form-data; name=\"" + fileKey
                    + "\"; filename=\"" + file.getName() + "\"" + LINE_END);
            sb.append("Content-Type:image/pjpeg" + LINE_END);
            sb.append(LINE_END);
            params = sb.toString();
            sb = null;

            Log.i(TAG, file.getName() + "=" + params + "##");
            dos.write(params.getBytes());

            InputStream is = new FileInputStream(file);
            onUploadProcessListener.initUpload((int) file.length());
            byte[] bytes = new byte[1024];
            int len = 0;
            int curLen = 0;
            while ((len = is.read(bytes)) != -1) {
                curLen += len;
                dos.write(bytes, 0, len);
                onUploadProcessListener.onUploadProcess(curLen);
            }
            is.close();

            dos.write(LINE_END.getBytes());
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
            dos.write(end_data);
            dos.flush();

            int res = conn.getResponseCode();
            responseTime = System.currentTimeMillis();
            this.requestTime = (int) ((responseTime - requestTime) / 1000);
            Log.e(TAG, "response code:" + res);
            if (res == 200) {
                Log.e(TAG, "request success");
                InputStream input = conn.getInputStream();
                StringBuffer sb1 = new StringBuffer();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb1.append((char) ss);
                }
                result = sb1.toString();
                Log.e(TAG, "result : " + result);
                sendMessage(UPLOAD_SUCCESS_CODE, "upload success:"
                        + result);
                return;
            } else {
                Log.e(TAG, "request error");
                sendMessage(UPLOAD_SERVER_ERROR_CODE, "upload fail:code=" + res);
                return;
            }
        } catch (MalformedURLException e) {
            sendMessage(UPLOAD_SERVER_ERROR_CODE, "upload fail:error=" + e.getMessage());
            e.printStackTrace();
            return;
        } catch (IOException e) {
            sendMessage(UPLOAD_SERVER_ERROR_CODE, "upload fail:error=" + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    private void sendMessage(int responseCode, String responseMessage) {
        onUploadProcessListener.onUploadDone(responseCode, responseMessage);
    }

    public static interface OnUploadProcessListener {

        void onUploadDone(int responseCode, String message);

        void onUploadProcess(int uploadSize);

        void initUpload(int fileSize);
    }

    private OnUploadProcessListener onUploadProcessListener;

    public void setOnUploadProcessListener(OnUploadProcessListener onUploadProcessListener) {
        this.onUploadProcessListener = onUploadProcessListener;
    }

    public static int getRequestTime() {
        return requestTime;
    }
}
