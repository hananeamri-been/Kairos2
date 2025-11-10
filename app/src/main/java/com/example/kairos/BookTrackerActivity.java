package com.example.kairos;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class BookTrackerActivity extends BaseActivity implements BookAdapter.OnSummaryClickListener {

    private EditText inputQuery;
    private ImageButton btnSearch;
    private ListView listSearchResults;
    private RecyclerView recycler;

    private final ArrayList<BookItem> myBooks = new ArrayList<>();
    private BookAdapter adapter;

    private final ArrayList<SearchResultItem> searchResults = new ArrayList<>();
    private SearchResultsAdapter searchAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_tracker);

        inputQuery = findViewById(R.id.inputQuery);
        btnSearch = findViewById(R.id.btnSearch);
        listSearchResults = findViewById(R.id.listSearchResults);
        recycler = findViewById(R.id.recyclerBooks);

        myBooks.addAll(BookStore.load(this));


        adapter = new BookAdapter(this, myBooks, position -> {
            BookItem b = myBooks.get(position);
            BookSummaryDialog dlg = new BookSummaryDialog(BookTrackerActivity.this, b.summary, text -> {
                b.summary = text;
                BookStore.save(BookTrackerActivity.this, myBooks);
            });
            dlg.show();
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);


        searchAdapter = new SearchResultsAdapter(searchResults);
        listSearchResults.setAdapter(searchAdapter);


        btnSearch.setOnClickListener(v -> doSearch());


        listSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResultItem sel = searchResults.get(position);
                BookItem item = new BookItem(sel.title, sel.buyUrl, sel.thumbUrl);
                myBooks.add(0, item);
                BookStore.save(BookTrackerActivity.this, myBooks);
                adapter.notifyItemInserted(0);
                recycler.scrollToPosition(0);


                searchResults.clear();
                searchAdapter.notifyDataSetChanged();
                listSearchResults.setVisibility(View.GONE);
                inputQuery.setText("");
            }
        });


        setupBottomBar();
        View nav = findViewById(R.id.nav_fun);
        if (nav != null) nav.setSelected(true);
    }

    private void doSearch() {
        String q = inputQuery.getText().toString().trim();
        if (TextUtils.isEmpty(q)) return;
        new BooksTask().execute(q);
    }


    private class BooksTask extends AsyncTask<String, Void, Void> {
        final ArrayList<SearchResultItem> tmp = new ArrayList<>();

        @Override
        protected Void doInBackground(String... strings) {
            String q = strings[0];
            HttpURLConnection conn = null;
            try {
                String urlStr = "https://www.googleapis.com/books/v1/volumes?q="
                        + URLEncoder.encode(q, "UTF-8")
                        + "&maxResults=10";
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject root = new JSONObject(sb.toString());
                JSONArray items = root.optJSONArray("items");
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject it  = items.getJSONObject(i);
                        JSONObject vol = it.optJSONObject("volumeInfo");
                        if (vol == null) continue;

                        String title = vol.optString("title");
                        if (TextUtils.isEmpty(title)) continue;

                        // thumbnail
                        String thumbUrl = null;
                        JSONObject imgs = vol.optJSONObject("imageLinks");
                        if (imgs != null) {
                            thumbUrl = imgs.optString("smallThumbnail", null);
                            if (TextUtils.isEmpty(thumbUrl)) thumbUrl = imgs.optString("thumbnail", null);
                            if (!TextUtils.isEmpty(thumbUrl) && thumbUrl.startsWith("http:")) {
                                thumbUrl = "https:" + thumbUrl.substring(5);
                            }
                        }

                        // lien Amazon direct (recherche interne Amazon)
                        String amazonDirect = "https://www.amazon.fr/s?k=" + URLEncoder.encode(title, "UTF-8");

                        tmp.add(new SearchResultItem(title, amazonDirect, thumbUrl));
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            searchResults.clear();
            searchResults.addAll(tmp);
            searchAdapter.notifyDataSetChanged();
            listSearchResults.setVisibility(searchResults.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onSummaryClick(int position) {

    }
}
