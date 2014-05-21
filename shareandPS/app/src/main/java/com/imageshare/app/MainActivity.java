package com.imageshare.app;
/**
 * Created by jaygo on 14-4-19.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imageshare.upload.UploadUtil;
import com.imageshare.upload.UploadUtil.OnUploadProcessListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements OnClickListener, OnUploadProcessListener {
    private static final String TAG = "MainActivity";
    private static final int TO_UPLOAD_FILE = 1;
    private static final int UPLOAD_FILE_DONE = 2;
    private static final int TO_SELECT_PHOTO = 3;
    private static final int UPLOAD_INIT_PROCESS = 4;
    private static final int UPLOAD_IN_PROCESS = 5;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TO_UPLOAD_FILE:
                    toUploadFile();
                    break;
                case UPLOAD_INIT_PROCESS:
                    progressBar.setMax(msg.arg1);
                    break;
                case UPLOAD_IN_PROCESS:
                    progressBar.setProgress(msg.arg1);
                    break;
                case UPLOAD_FILE_DONE:
                    String result = "上传回应码：" + msg.arg1 + "\n上传结果：" + msg.obj + "\n上传耗时：" + UploadUtil.getRequestTime() + "s";
                    uploadImageResult.setText(result);
                    break;
                //   case TO_CHECK_STATUS:
                //       toCheckStatus();
                //       break;
                //   case CHECK_DONE:
                //       Toast.makeText(getApplicationContext(), (CharSequence) msg.obj, 1).show();
                //       break;
                default:
                    break;
            }
        }
    };
    private static final int TO_CHECK_STATUS = 6;
    public static String picPath = null;
    static TextView txt;
    //private static String requestURL = "http://192.168.1.100:8080/MyTest/p/file!upload";
    private static String requestURL = "http://192.168.61.106:8080/MyTest/p/file!upload";
    private Button uploadButton, quitButton, checkButton, friendButton;
    private ImageView imageView;
    private TextView uploadImageResult;
    private ProgressBar progressBar;
    private ImageButton cameraButon;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadImageResult = (TextView) findViewById(R.id.uploadImageResult);
        txt = (TextView) findViewById(R.id.txt1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        uploadButton = (Button) findViewById(R.id.uploadImage);
        checkButton = (Button) findViewById(R.id.checkImage);
        friendButton = (Button) findViewById(R.id.friendImage);
        imageView = (ImageView) findViewById(R.id.imageView);
        cameraButon = (ImageButton) findViewById(R.id.camera);
        quitButton = (Button) findViewById(R.id.back);
        quitButton.setOnClickListener(this);
        cameraButon.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        checkButton.setOnClickListener(this);
        friendButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                toQuit();
                break;
            case R.id.camera:
                Intent intent2 = new Intent(this, SelectPicActivity.class);
                startActivityForResult(intent2, TO_SELECT_PHOTO);
                break;
            case R.id.uploadImage:
                if (picPath != null) {
                    handler.sendEmptyMessage(TO_UPLOAD_FILE);
                } else {
                    Toast.makeText(this, "文件路径为空，请先选择图片！", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.friendImage:
                Intent intent3 = new Intent(this, FriendCycleActivity.class);
                startActivity(intent3);
                break;
            case R.id.checkImage:
                handler.sendEmptyMessage(TO_CHECK_STATUS);
                break;
        }
    }

    public void toQuit() {
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setMessage("确定退出吗?");
        builder.setTitle("提示");
        builder.setPositiveButton("否",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton("是",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TO_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            imageView.setImageBitmap(null);
            picPath = null;
            picPath = data.getStringExtra(SelectPicActivity.KEY_PHOTO_PATH);
            //  Bitmap bm = BitmapFactory.decodeFile(picPath);
            //  BitmapRegionDecoder bm = BitmapRegionDecoder.newInstance(picPath, false);
            txt.setText("文件路径：" + picPath);
            Log.i(TAG, "文件路径=" + picPath);
            //  imageView.setImageBitmap(bm);
        }
    }

    private void toUploadFile() {
        uploadImageResult.setText("上传结果...");
        progressDialog.setMessage("上传中...");
        progressDialog.setTitle("上传文件：");
        progressDialog.setIcon(R.drawable.app_icon);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        String fileKey = "img";
        UploadUtil uploadUtil = UploadUtil.getInstance();
        uploadUtil.setOnUploadProcessListener(this);
        Map<String, String> params = new HashMap<String, String>();
        params.put("orderId", "111");
        uploadUtil.uploadFile(picPath, fileKey, requestURL, params);
    }

    public void onUploadProcess(int uploadSize) {
        Message msg = Message.obtain();
        msg.what = UPLOAD_IN_PROCESS;
        msg.arg1 = uploadSize;
        handler.sendMessage(msg);
    }

    public void initUpload(int fileSize) {
        Message msg = Message.obtain();
        msg.what = UPLOAD_INIT_PROCESS;
        msg.arg1 = fileSize;
        handler.sendMessage(msg);
    }

    public void onUploadDone(int responseCode, String message) {
        progressDialog.dismiss();
        Message msg = Message.obtain();
        msg.what = UPLOAD_FILE_DONE;
        msg.arg1 = responseCode;
        msg.obj = message;
        handler.sendMessage(msg);
    }

}

