package com.imageshare.app;
/**
 * Created by jaygo on 14-4-19.
 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SelectPicActivity extends Activity implements OnClickListener {

    public static final int SELECT_PIC_BY_TAKE_PHOTO = 1;
    public static final int SELECT_PIC_BY_PICK_PHOTO = 2;
    public static final String KEY_PHOTO_PATH = "photo_path";
    private static final String TAG = "SelectPicActivity";
    private LinearLayout dialogLayout;
    private Button takePhotoBtn, pickPhotoBtn, cancelBtn;
    private String picPath;
    private Intent lastIntent;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_pic_layout);

        dialogLayout = (LinearLayout) findViewById(R.id.dialog_layout);
        dialogLayout.setOnClickListener(this);
        takePhotoBtn = (Button) findViewById(R.id.btn_take_photo);
        takePhotoBtn.setOnClickListener(this);
        pickPhotoBtn = (Button) findViewById(R.id.btn_pick_photo);
        pickPhotoBtn.setOnClickListener(this);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(this);
        lastIntent = getIntent();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_layout:
                finish();
                break;
            case R.id.btn_take_photo:
                takePhoto();
                break;
            case R.id.btn_pick_photo:
                pickPhoto();
                break;
            default:
                finish();
                break;
        }
    }

    private void takePhoto() {
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//"android.media.action.IMAGE_CAPTURE"
            ContentValues values = new ContentValues();
            photoUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, SELECT_PIC_BY_TAKE_PHOTO);
        } else {
            Toast.makeText(this, "SD卡没有挂载！", Toast.LENGTH_LONG).show();
        }
    }

    private void pickPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            doPhoto(requestCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doPhoto(int requestCode, Intent data) {
        if (requestCode == SELECT_PIC_BY_PICK_PHOTO) {
            if (data == null) {
                Toast.makeText(this, "没有拍到照片！", Toast.LENGTH_LONG).show();
                return;
            }
            photoUri = data.getData();
            Log.e(TAG, "所选图片Uri: " + photoUri.toString());
            if (photoUri == null) {
                Toast.makeText(this, "不能获取相片URI！", Toast.LENGTH_LONG).show();
                return;
            }
        }
        String[] pojo = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(photoUri, pojo, null, null, null);
        if (cursor != null) {
            Log.e(TAG, "数据流：" + pojo);
            int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
            Log.e(TAG, "pojo[0]列的首地址：" + columnIndex);
            cursor.moveToFirst();
            picPath = cursor.getString(columnIndex);
            Log.e(TAG, "真实路径：" + picPath);
            cursor.close();
        }
        if (picPath != null) {
            lastIntent.putExtra(KEY_PHOTO_PATH, picPath);
            setResult(Activity.RESULT_OK, lastIntent);
            finish();
        } else {
            Toast.makeText(this, "没有找到图片路径！", Toast.LENGTH_LONG).show();
        }
    }
}
