package com.example.kairos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WatchAdapter extends RecyclerView.Adapter<WatchAdapter.VH> {

    public interface OnCommentClickListener {
        void onCommentClick(int position);
    }

    private final Context context;
    private final List<WatchItem> data;
    private final OnCommentClickListener commentClick;
    private final int tabType; // WatchStore.TAB_*

    public WatchAdapter(Context context, List<WatchItem> data, int tabType, OnCommentClickListener commentClick) {
        this.context = context;
        this.data = data;
        this.tabType = tabType;
        this.commentClick = commentClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watch_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        WatchItem w = data.get(position);

        // Poster
        h.imgPoster.setImageDrawable(null);
        h.imgPoster.setTag(w.thumbUrl);
        if (w.thumbUrl != null && !w.thumbUrl.isEmpty()) {
            new ImageTask(h.imgPoster).execute(w.thumbUrl);
        }

        // Title
        h.txtTitle.setText(w.title);

        // Watched
        h.chWatched.setOnCheckedChangeListener(null);
        h.chWatched.setChecked(w.watched);
        h.chWatched.setOnCheckedChangeListener((buttonView, isChecked) -> {
            w.watched = isChecked;
            WatchStore.save(context, data, tabType);
        });

        // Rating
        h.rating.setOnRatingBarChangeListener(null);
        h.rating.setRating(w.rating);
        h.rating.setOnRatingBarChangeListener((rb, val, fromUser) -> {
            if (fromUser) {
                w.rating = (int) val;
                WatchStore.save(context, data, tabType);
            }
        });

        // Comment dialog
        h.btnComment.setOnClickListener(v -> {
            if (commentClick != null) commentClick.onCommentClick(h.getBindingAdapterPosition());
        });

        // Long press = delete
        h.itemView.setOnLongClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return true;
            new AlertDialog.Builder(context)
                    .setTitle("Delete this item?")
                    .setMessage(w.title)
                    .setPositiveButton("Delete", (d, which) -> {
                        data.remove(pos);
                        notifyItemRemoved(pos);
                        WatchStore.save(context, data, tabType);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgPoster; TextView txtTitle; CheckBox chWatched; RatingBar rating; ImageButton btnComment;
        VH(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle  = itemView.findViewById(R.id.txtTitle);
            chWatched = itemView.findViewById(R.id.chWatched);
            rating    = itemView.findViewById(R.id.rating);
            btnComment= itemView.findViewById(R.id.btnComment);
        }
    }

    // Loader d'image simple (sans Glide/Picasso)
    static class ImageTask extends android.os.AsyncTask<String, Void, Bitmap> {
        private final ImageView target; private String url;
        ImageTask(ImageView target){ this.target = target; }
        @Override protected Bitmap doInBackground(String... s){
            url = s[0];
            try { return BitmapFactory.decodeStream(new java.net.URL(url).openStream()); }
            catch(Exception e){ return null; }
        }
        @Override protected void onPostExecute(Bitmap bmp){
            if (bmp != null && url.equals(target.getTag())) target.setImageBitmap(bmp);
        }
    }
}
