package com.example.kairos;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

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

public class WatchTrackerActivity extends BaseActivity {

    private Button btnMovies, btnSeries, btnAnime;
    private int tabType = WatchStore.TAB_MOVIE; // 0=Movies, 1=Series, 2=Anime

    private EditText inputQuery;
    private ImageButton btnSearch;
    private ListView listSearchResults;
    private RecyclerView recycler;

    private final ArrayList<WatchItem> myItems = new ArrayList<>();
    private WatchAdapter adapter;

    private final ArrayList<SearchResultMediaItem> searchResults = new ArrayList<>();
    private SearchResultsMediaAdapter searchAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_tracker);

        btnMovies = findViewById(R.id.btnMovies);
        btnSeries = findViewById(R.id.btnSeries);
        btnAnime  = findViewById(R.id.btnAnime);

        inputQuery = findViewById(R.id.inputQuery);
        btnSearch = findViewById(R.id.btnSearch);
        listSearchResults = findViewById(R.id.listSearchResults);
        recycler = findViewById(R.id.recyclerWatch);

        // Tabs
        View.OnClickListener tabClick = v -> {
            int prev = tabType;
            if (v.getId() == R.id.btnMovies) tabType = WatchStore.TAB_MOVIE;
            else if (v.getId() == R.id.btnSeries) tabType = WatchStore.TAB_SERIES;
            else if (v.getId() == R.id.btnAnime)  tabType = WatchStore.TAB_ANIME;

            if (tabType != prev) {
                updateTabsUI();
                reloadList();
            }
        };
        btnMovies.setOnClickListener(tabClick);
        btnSeries.setOnClickListener(tabClick);
        btnAnime.setOnClickListener(tabClick);
        updateTabsUI();

        // Liste principale
        myItems.addAll(WatchStore.load(this, tabType));
        adapter = new WatchAdapter(this, myItems, tabType, position -> {
            WatchItem it = myItems.get(position);
            MediaCommentDialog dlg = new MediaCommentDialog(WatchTrackerActivity.this, it.comment, text -> {
                it.comment = text;
                WatchStore.save(WatchTrackerActivity.this, myItems, tabType);
            });
            dlg.show();
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // Résultats de recherche
        searchAdapter = new SearchResultsMediaAdapter(searchResults);
        listSearchResults.setAdapter(searchAdapter);

        btnSearch.setOnClickListener(v -> doSearch());

        listSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResultMediaItem sel = searchResults.get(position);
                WatchItem item = new WatchItem(sel.title, sel.thumbUrl);
                myItems.add(0, item);
                WatchStore.save(WatchTrackerActivity.this, myItems, tabType);
                adapter.notifyItemInserted(0);
                recycler.scrollToPosition(0);

                // clear/hide results + clear query
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

    private void updateTabsUI() {
        btnMovies.setEnabled(tabType != WatchStore.TAB_MOVIE);
        btnSeries.setEnabled(tabType != WatchStore.TAB_SERIES);
        btnAnime.setEnabled(tabType != WatchStore.TAB_ANIME);

        btnMovies.setAlpha(tabType == WatchStore.TAB_MOVIE ? 1f : 0.6f);
        btnSeries.setAlpha(tabType == WatchStore.TAB_SERIES ? 1f : 0.6f);
        btnAnime.setAlpha(tabType == WatchStore.TAB_ANIME ? 1f : 0.6f);
    }

    private void reloadList() {
        myItems.clear();
        myItems.addAll(WatchStore.load(this, tabType));
        adapter = new WatchAdapter(this, myItems, tabType, position -> {
            WatchItem it = myItems.get(position);
            MediaCommentDialog dlg = new MediaCommentDialog(WatchTrackerActivity.this, it.comment, text -> {
                it.comment = text;
                WatchStore.save(WatchTrackerActivity.this, myItems, tabType);
            });
            dlg.show();
        });
        recycler.setAdapter(adapter);

        searchResults.clear();
        searchAdapter.notifyDataSetChanged();
        listSearchResults.setVisibility(View.GONE);
        inputQuery.setText("");
    }

    private void doSearch() {
        String q = inputQuery.getText().toString().trim();
        if (TextUtils.isEmpty(q)) {
            Toast.makeText(this, "Enter a title to search", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (tabType) {
            case WatchStore.TAB_MOVIE:
                new ITunesMoviesTask().execute(q); // Films via iTunes (sans clé)
                break;
            case WatchStore.TAB_SERIES:
                new TvMazeTask().execute(q);       // Séries via TVMaze
                break;
            case WatchStore.TAB_ANIME:
                new JikanTask().execute(q);        // Anime via Jikan
                break;
        }
    }

    /** iTunes Search: FILMS (sans clé), multi-pass pour fiabiliser */
    private class ITunesMoviesTask extends AsyncTask<String, Void, String> {
        final ArrayList<SearchResultMediaItem> tmp = new ArrayList<>();
        @Override protected String doInBackground(String... strings) {
            String q = strings[0];
            HttpURLConnection conn = null;
            try {
                String[] urls = new String[] {
                        "https://itunes.apple.com/search?term=" + URLEncoder.encode(q, "UTF-8")
                                + "&media=movie&entity=movie&country=us&limit=25&attribute=movieTerm",
                        "https://itunes.apple.com/search?term=" + URLEncoder.encode(q, "UTF-8")
                                + "&media=movie&entity=movie&country=us&limit=25",
                        "https://itunes.apple.com/search?term=" + URLEncoder.encode(q, "UTF-8")
                                + "&media=all&country=us&limit=25"
                };

                for (String urlStr : urls) {
                    URL url = new URL(urlStr);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(12000);
                    conn.setReadTimeout(12000);

                    int code = conn.getResponseCode();
                    InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line; while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    if (code < 200 || code >= 300) continue;

                    JSONObject root = new JSONObject(sb.toString());
                    JSONArray results = root.optJSONArray("results");
                    if (results == null || results.length() == 0) continue;

                    int before = tmp.size();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject it = results.getJSONObject(i);
                        String title = it.optString("trackName");
                        if (TextUtils.isEmpty(title)) title = it.optString("collectionName");

                        String art = it.optString("artworkUrl100", null);
                        if (TextUtils.isEmpty(art)) art = it.optString("artworkUrl60", null);
                        if (!TextUtils.isEmpty(art)) art = art.replace("100x100bb", "200x200bb");

                        if (!TextUtils.isEmpty(title)) tmp.add(new SearchResultMediaItem(title, art));
                    }
                    if (tmp.size() > before) break;
                }
                return null;
            } catch (Exception e) {
                return e.getClass().getSimpleName() + ": " + e.getMessage();
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        @Override protected void onPostExecute(String error) {
            if (error != null) {
                Toast.makeText(WatchTrackerActivity.this, "Movies search: " + error, Toast.LENGTH_LONG).show();
                listSearchResults.setVisibility(View.GONE);
                return;
            }
            searchResults.clear();
            searchResults.addAll(tmp);
            searchAdapter.notifyDataSetChanged();
            listSearchResults.setVisibility(searchResults.isEmpty() ? View.GONE : View.VISIBLE);
            if (searchResults.isEmpty()) {
                Toast.makeText(WatchTrackerActivity.this, "No results", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** TVMaze: SÉRIES (sans clé) */
    private class TvMazeTask extends AsyncTask<String, Void, String> {
        final ArrayList<SearchResultMediaItem> tmp = new ArrayList<>();
        @Override protected String doInBackground(String... strings) {
            String q = strings[0];
            HttpURLConnection conn = null;
            try {
                String urlStr = "https://api.tvmaze.com/search/shows?q=" + URLEncoder.encode(q, "UTF-8");
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line; while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                if (code < 200 || code >= 300) return "HTTP " + code + " : " + sb;

                JSONArray results = new JSONArray(sb.toString());
                for (int i = 0; i < results.length(); i++) {
                    JSONObject wrapper = results.getJSONObject(i);
                    JSONObject show = wrapper.optJSONObject("show");
                    if (show == null) continue;
                    String title = show.optString("name");
                    String thumb = null;
                    JSONObject image = show.optJSONObject("image");
                    if (image != null) {
                        thumb = image.optString("medium", null);
                        if (TextUtils.isEmpty(thumb)) thumb = image.optString("original", null);
                    }
                    if (!TextUtils.isEmpty(title)) {
                        tmp.add(new SearchResultMediaItem(title, thumb));
                    }
                }
                return null;
            } catch (Exception e) {
                return e.getClass().getSimpleName() + ": " + e.getMessage();
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        @Override protected void onPostExecute(String error) {
            if (error != null) {
                Toast.makeText(WatchTrackerActivity.this, "Series search: " + error, Toast.LENGTH_LONG).show();
                listSearchResults.setVisibility(View.GONE);
                return;
            }
            searchResults.clear();
            searchResults.addAll(tmp);
            searchAdapter.notifyDataSetChanged();
            listSearchResults.setVisibility(searchResults.isEmpty() ? View.GONE : View.VISIBLE);
            if (searchResults.isEmpty()) {
                Toast.makeText(WatchTrackerActivity.this, "No results", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Jikan: ANIME (sans clé) */
    private class JikanTask extends AsyncTask<String, Void, String> {
        final ArrayList<SearchResultMediaItem> tmp = new ArrayList<>();
        @Override protected String doInBackground(String... strings) {
            String q = strings[0];
            HttpURLConnection conn = null;
            try {
                String urlStr = "https://api.jikan.moe/v4/anime?q=" + URLEncoder.encode(q, "UTF-8")
                        + "&limit=20&sfw";
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line; while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                if (code < 200 || code >= 300) return "HTTP " + code + " : " + sb;

                JSONObject root = new JSONObject(sb.toString());
                JSONArray data = root.optJSONArray("data");
                if (data != null) {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject it = data.getJSONObject(i);
                        String title = it.optString("title");
                        String thumb = null;
                        JSONObject images = it.optJSONObject("images");
                        if (images != null) {
                            JSONObject jpg = images.optJSONObject("jpg");
                            if (jpg != null) thumb = jpg.optString("image_url", null);
                        }
                        if (!TextUtils.isEmpty(title)) {
                            tmp.add(new SearchResultMediaItem(title, thumb));
                        }
                    }
                }
                return null;
            } catch (Exception e) {
                return e.getClass().getSimpleName() + ": " + e.getMessage();
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        @Override protected void onPostExecute(String error) {
            if (error != null) {
                Toast.makeText(WatchTrackerActivity.this, "Anime search: " + error, Toast.LENGTH_LONG).show();
                listSearchResults.setVisibility(View.GONE);
                return;
            }
            searchResults.clear();
            searchResults.addAll(tmp);
            searchAdapter.notifyDataSetChanged();
            listSearchResults.setVisibility(searchResults.isEmpty() ? View.GONE : View.VISIBLE);
            if (searchResults.isEmpty()) {
                Toast.makeText(WatchTrackerActivity.this, "No results", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
