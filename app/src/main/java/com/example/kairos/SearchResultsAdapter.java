package com.example.kairos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class SearchResultsAdapter extends BaseAdapter {
    private final List<SearchResultItem> data;

    public SearchResultsAdapter(List<SearchResultItem> data) {
        this.data = data;
    }

    @Override public int getCount() { return data.size(); }
    @Override public Object getItem(int position) { return data.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH h;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_book_result, parent, false);
            h = new VH(convertView);
            convertView.setTag(h);
        } else {
            h = (VH) convertView.getTag();
        }

        SearchResultItem it = data.get(position);
        h.title.setText(it.title);

        // charge la miniature (naïf, suffisant pour une liste courte)
        h.thumb.setImageDrawable(null);
        h.thumb.setTag(it.thumbUrl); // pour éviter le clignotement
        if (!TextUtils.isEmpty(it.thumbUrl)) {
            new ImageTask(h.thumb).execute(it.thumbUrl);
        }

        return convertView;
    }

    static class VH {
        ImageView thumb;
        TextView title;
        VH(View v) {
            thumb = v.findViewById(R.id.imgThumb);
            title = v.findViewById(R.id.txtResultTitle);
        }
    }

    static class ImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView target;
        private String url;

        ImageTask(ImageView target) {
            this.target = target;
        }

        @Override protected Bitmap doInBackground(String... strings) {
            url = strings[0];
            try (InputStream is = new URL(url).openStream()) {
                return BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                return null;
            }
        }

        @Override protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null && url.equals(target.getTag())) {
                target.setImageBitmap(bitmap);
            }
        }
    }
}
