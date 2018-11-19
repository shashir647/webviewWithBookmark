package com.mobile.shashi_ranjan.dummybrowseapp.activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobile.shashi_ranjan.dummybrowseapp.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mobile.shashi_ranjan.dummybrowseapp.activity.MainActivity.PREFERENCES;
import static com.mobile.shashi_ranjan.dummybrowseapp.activity.MainActivity.WEB_LINKS;
public class SavedBookmark extends AppCompatActivity {
    private ArrayList<HashMap<String, String>> listRowData;
    public static String TAG_LINK = "link";
    private ListAdapter adapter;
    @BindView(R.id.list_view_bookmarks)ListView mListBookmarks;
    @BindView(R.id.lin_emptyList)LinearLayout mLinEmptyList;
    @BindView(R.id.swipeToRefresh)SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.toolbar)Toolbar mToolbar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_bookmark);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.bookmark);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        mToolbar.setNavigationOnClickListener(v -> finish());
        mSwipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(() -> new LoadBookmarks().execute());

        new LoadBookmarks().execute();
        mListBookmarks.setOnItemClickListener((parent, view, position, id) -> {

            Object o = mListBookmarks.getAdapter().getItem(position);
            if (o instanceof Map) {
                Map map = (Map) o;
                Intent in = new Intent(SavedBookmark.this, MainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                in.putExtra("url", String.valueOf(map.get(TAG_LINK)));
                startActivity(in);
                deleteBookmark(String.valueOf(map.get(TAG_LINK)));
            }
        });
    }

     private class LoadBookmarks extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            // updating UI from Background Thread
            runOnUiThread(() -> {

                SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String jsonLink = sharedPreferences.getString(WEB_LINKS, null);
                assert jsonLink != null;
                jsonLink=jsonLink.replace("null,","");
                listRowData = new ArrayList<>();

                if (jsonLink != null) {

                    Gson gson = new Gson();
                    ArrayList<String> linkArray = gson.fromJson(jsonLink, new TypeToken<ArrayList<String>>() {
                    }.getType());
                    for (int i = 0; i < linkArray.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(TAG_LINK, linkArray.get(i));
                        listRowData.add(map);
                    }
                    adapter = new SimpleAdapter(SavedBookmark.this,
                            listRowData, R.layout.bookmark_list_row,
                            new String[]{TAG_LINK},
                            new int[]{R.id.tv_link});
                    mListBookmarks.setAdapter(adapter);
                }
                mLinEmptyList.setVisibility(View.VISIBLE);
                mListBookmarks.setEmptyView(mLinEmptyList);


            });
            return null;
        }

        protected void onPostExecute(String args) {

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void deleteBookmark(String link) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String jsonLink = sharedPreferences.getString(WEB_LINKS, null);
        if (jsonLink != null) {


            Gson gson = new Gson();
            ArrayList<String> linkArray = gson.fromJson(jsonLink, new TypeToken<ArrayList<String>>() {
            }.getType());

            linkArray.remove(link);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(WEB_LINKS, new Gson().toJson(linkArray));
            editor.apply();

            new LoadBookmarks().execute();
        }
    }

}