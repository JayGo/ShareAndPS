package com.imageshare.app;
/**
 * Created by jaygo on 14-4-19.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.imageshare.download.CacheUtil;
import com.imageshare.download.CacheUtil.OnCacheProcessListener;
import com.imageshare.download.FileInfo;
import com.imageshare.download.ImageAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FriendCycleActivity extends Activity implements OnClickListener, OnCacheProcessListener {
    private final static String TAG = "FriendCycleActivity";
    //private final String requestURL =  "http://192.168.1.100:8080/MyTest/listsCompress.xml";
    private final String requestURL = "http://192.168.61.106:8080/MyTest/listsCompress.xml";
    private final int TO_REFRESH = 1;
    private final int CACHE_DONE_SUCCESS = 2;
    private final int CACHE_FAIL = 3;
    private File cache;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TO_REFRESH:
                    toRefresh();
                    break;

                case CACHE_FAIL:
                    Toast.makeText(getApplicationContext(), "请检查网络连接！", 9).show();
                    break;

                case CACHE_DONE_SUCCESS:
                    List<FileInfo> fileInfos1 = (ArrayList<FileInfo>) msg.obj;
                    toAdapt(getApplicationContext(), fileInfos1, cache);
                    break;
            }
        }
    };
    private ListView mListView;
    private ImageAdapter mAdapter;
    private Button reFreshButton;
    private Button backButton;
    private List<FileInfo> fileInfos;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.friend_cycle);

        mListView = (ListView) findViewById(R.id.listView);
        reFreshButton = (Button) findViewById(R.id.refreshImage);
        backButton = (Button) findViewById(R.id.back);
        reFreshButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        cache = new File(Environment.getExternalStorageDirectory(), "cache");

        if (!cache.exists()) {
            cache.mkdirs();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                Intent intent1 = new Intent(this, MainActivity.class);
                startActivity(intent1);
                break;

            case R.id.refreshImage:
                toRefresh();
                break;
        }
    }

    private void toRefresh() {
        fileInfos = null;
        CacheUtil cacheUtil = CacheUtil.getInstance();
        cacheUtil.setOnCacheProccessListener(this);
        cacheUtil.cacheFile(requestURL);
    }


    @Override
    public void onCacheDone(int responseCode, List<FileInfo> fileInfos) {
        Message msg = Message.obtain();
        msg.what = CACHE_DONE_SUCCESS;
        msg.arg1 = responseCode;
        msg.obj = fileInfos;
        handler.sendMessage(msg);
    }

    @Override
    public void onCacheFail(int responseCode, String tip) {
        Message msg = Message.obtain();
        msg.what = CACHE_FAIL;
        msg.arg1 = responseCode;
        msg.obj = tip;
        handler.sendMessage(msg);
    }

    private void toAdapt(Context context, List<FileInfo> fileInfos, File cache) {
        mAdapter = new ImageAdapter(context, fileInfos, cache);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File[] files = cache.listFiles();
        for (File file : files) {
            file.delete();
        }
        cache.delete();
    }
}

