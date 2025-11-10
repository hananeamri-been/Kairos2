package com.example.kairos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.VH> {

    public interface OnSummaryClickListener {
        void onSummaryClick(int position);
    }

    private final Context context;
    private final List<BookItem> data;
    private final OnSummaryClickListener summaryClickListener;

    public BookAdapter(Context context, List<BookItem> data, OnSummaryClickListener listener) {
        this.context = context;
        this.data = data;
        this.summaryClickListener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BookItem b = data.get(position);


        h.imgPoster.setImageDrawable(null);
        h.imgPoster.setTag(b.thumbUrl);
        if (b.thumbUrl != null && !b.thumbUrl.isEmpty()) {
            new ImageTask(h.imgPoster).execute(b.thumbUrl);
        }


        h.txtTitle.setText(b.title);


        h.chRead.setOnCheckedChangeListener(null);
        h.chRead.setChecked(b.read);
        h.chRead.setOnCheckedChangeListener((buttonView, isChecked) -> {
            b.read = isChecked;
            BookStore.save(context, data);
        });


        h.rating.setOnRatingBarChangeListener(null);
        h.rating.setRating(b.rating);
        h.rating.setOnRatingBarChangeListener((rb, val, fromUser) -> {
            if (fromUser) {
                b.rating = (int) val;
                BookStore.save(context, data);
            }
        });


        h.btnSummary.setOnClickListener(v -> {
            if (summaryClickListener != null) {
                summaryClickListener.onSummaryClick(h.getBindingAdapterPosition());
            }
        });


        h.btnBuy.setOnClickListener(v -> {
            if (b.buyUrl != null && !b.buyUrl.isEmpty()) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(b.buyUrl));
                context.startActivity(i);
            }
        });


        h.itemView.setOnLongClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return true;
            new AlertDialog.Builder(context)
                    .setTitle("Supprimer ce livre ?")
                    .setMessage(b.title)
                    .setPositiveButton("Supprimer", (d, which) -> {
                        data.remove(pos);
                        notifyItemRemoved(pos);
                        BookStore.save(context, data);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle;
        CheckBox chRead;
        RatingBar rating;
        ImageButton btnSummary;
        Button btnBuy;

        VH(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle  = itemView.findViewById(R.id.txtTitle);
            chRead    = itemView.findViewById(R.id.chRead);
            rating    = itemView.findViewById(R.id.rating);
            btnSummary= itemView.findViewById(R.id.btnSummary);
            btnBuy    = itemView.findViewById(R.id.btnBuy);
        }
    }


    static class ImageTask extends android.os.AsyncTask<String, Void, Bitmap> {
        private final ImageView target;
        private String url;
        ImageTask(ImageView target) { this.target = target; }

        @Override
        protected Bitmap doInBackground(String... strings) {
            url = strings[0];
            try {
                return BitmapFactory.decodeStream(new java.net.URL(url).openStream());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            if (bmp != null && url.equals(target.getTag())) {
                target.setImageBitmap(bmp);
            }
        }
    }
}
