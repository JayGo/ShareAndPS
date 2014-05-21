package com.imageshare.download;
/**
 * Created by jaygo on 14-4-21.
 */
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.imageshare.app.R;

import java.io.File;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<FileInfo> fileInfos;
    private File cache;
    private LayoutInflater mInflater;

    public ImageAdapter(Context context, List<FileInfo> fileInfos, File cache) {
        this.context = context;
        this.fileInfos = fileInfos;
        this.cache = cache;

        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return fileInfos.size();
    }

    public Object getItem(int position) {
        return fileInfos.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.item, null);
        }

        ImageView iv_header = (ImageView) view.findViewById(R.id.imageView);
        TextView tv_name = (TextView) view.findViewById(R.id.textView);

        FileInfo fileInfo = fileInfos.get(position);
        asyncloadImage(iv_header, fileInfo.image);
        tv_name.setText(fileInfo.name);
        return view;
    }

    private void asyncloadImage(ImageView iv_header, String path) {
        CacheUtil cacheUtil = CacheUtil.getInstance();
        AsyncImageTask task = new AsyncImageTask(cacheUtil, iv_header);
        task.execute(path);
    }

    private final class AsyncImageTask extends AsyncTask<String, Integer, Uri> {
        private CacheUtil cacheUtil;
        private ImageView iv_header;

        public AsyncImageTask(CacheUtil cacheUtil, ImageView iv_header) {
            this.cacheUtil = cacheUtil;
            this.iv_header = iv_header;
        }

        @Override
        protected Uri doInBackground(String... params) {
            try {
                return cacheUtil.getImageURI(params[0], cache);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Uri result) {
            super.onPostExecute(result);
            if (iv_header != null && result != null) {
                iv_header.setImageURI(result);
            }
        }
    }
}