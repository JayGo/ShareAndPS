package com.imageshare.download;
/**
 * Created by jaygo on 14-4-20.
 */
import android.net.Uri;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CacheUtil {
    private static CacheUtil cacheUtil;
    private int readTimeOut = 10*1000;
    private int connectTimeout = 10*1000;
    private final static String TAG = "CacheUtil";

    public static CacheUtil getInstance() {
        if (cacheUtil == null) {
            cacheUtil = new CacheUtil();
        }
        return cacheUtil;
    }

    public void cacheFile(final String requestURL) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    toCacheFile(requestURL);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }

    public void toCacheFile(String requestURL) throws XmlPullParserException {
        try {
            List<FileInfo> fileInfos = null;
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeOut);
            conn.setRequestMethod("GET");
            int res = conn.getResponseCode();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                fileInfos = xmlParser(is);
                sendResult(res, fileInfos);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "未连接至服务器！");
            sendException(9, "请检查网络连接！");
        } catch (IOException e) {
            Log.e(TAG, "未连接至服务器！");
            sendException(9, "请检查网络连接！");
        }
    }

    public List<FileInfo> xmlParser(InputStream is) throws XmlPullParserException, IOException {
        List<FileInfo> fileInfos = null;
        FileInfo fileInfo = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "UTF-8");
        int eventType = parser.getEventType();
        while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("contacts")) {
                        fileInfos = new ArrayList<FileInfo>();
                    } else if (parser.getName().equals("contact")) {
                        fileInfo = new FileInfo();
                        fileInfo.setId(Integer.valueOf(parser.getAttributeValue(0)));
                    } else if (parser.getName().equals("name")) {
                        fileInfo.setName(parser.nextText());
                    } else if (parser.getName().equals("image")) {
                        fileInfo.setImage(parser.getAttributeValue(0));
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("contact")) {
                        fileInfos.add(fileInfo);
                    }
                    break;
            }
        }
        return fileInfos;
    }

    public Uri getImageURI(String path, File cache) throws Exception {
        String name = MD5.getMD5(path) + path.substring(path.lastIndexOf("."));
        File file = new File(cache, name);
        if (file.exists()) {
            return Uri.fromFile(file);
        } else {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
                return Uri.fromFile(file);
            }
        }
        return null;
    }

    private void sendResult(int responseCode, List<FileInfo> fileInfos) {
        onCacheProcessListener.onCacheDone(responseCode, fileInfos);
    }

    private void sendException(int responseCode, String tip) {
        onCacheProcessListener.onCacheFail(responseCode, tip);
    }

    public static interface OnCacheProcessListener {
        void onCacheDone(int responseCode, List<FileInfo> fileInfos);

        void onCacheFail(int responseCode, String tip);
    }

    private OnCacheProcessListener onCacheProcessListener;

    public void setOnCacheProccessListener(OnCacheProcessListener onCacheProcessListener) {
        this.onCacheProcessListener = onCacheProcessListener;
    }
}

