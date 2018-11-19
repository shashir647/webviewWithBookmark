package com.mobile.shashi_ranjan.dummybrowseapp.activity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.util.PatternsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobile.shashi_ranjan.dummybrowseapp.R;
import java.util.ArrayList;
import java.util.regex.Matcher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
public class MainActivity extends AppCompatActivity{
    private Context mContext;
    private boolean doubleBackToExitPressedOnce = false;
    @BindView(R.id.main_web_view)WebView webView;
    public static final String PREFERENCES = "My_Preference";
    public static final String WEB_LINKS = "links";
    private String current_page_url = getString(R.string.str_url);
    @BindView(R.id.main_progressBar)
    ProgressBar mainProgressBar;
    @BindView(R.id.main_content)
    RelativeLayout coordinatorLayout;
    @BindView(R.id.lin_bottom_item)
    LinearLayout lin_bottom_item;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_bookmark)
    TextView tvBookmark;
    @BindView(R.id.et_search)
    EditText etSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=getApplicationContext();
        ButterKnife.bind(this);
        onClickEventBottomView();
        if (getIntent().getExtras() != null) {
            current_page_url = getIntent().getStringExtra("url");
            initWebView(current_page_url);
        }
        webView.loadUrl(current_page_url);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                WebView webView = (WebView) v;

                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        if (webView.canGoBack()) {
                            webView.goBack();
                            return true;
                        }
                        break;
                }

            }

            return false;
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @OnClick(R.id.btn_go)
    public void selectGo(){
        validateURL(etSearch.getText().toString());

    }

    private void validateURL(String url) {
        Matcher m = PatternsCompat.WEB_URL.matcher(url);
        if (m.find()) {
            url = m.group();
            if (!(url.contains(".com") || url.contains(".in") || url.contains(".org"))) {
                initWebView(url);
            } else {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                    initWebView(url);
                } else {
                    initWebView(url);
                }
            }
        }else {
            url="https://www.google.com/search?q="+url;
            initWebView(url);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        current_page_url=url;
        webView.loadUrl(current_page_url);
    }
   private class MyWebViewClient extends WebViewClient {
       @Override
       public boolean shouldOverrideUrlLoading(WebView view, String url) {
           view.loadUrl(url);
           return true;

       }

       @Override
       public void onPageFinished(WebView view, String url) {
           mainProgressBar.setVisibility(View.GONE);
           super.onPageFinished(view, url);
           invalidateOptionsMenu();
       }

       @Override
       public void onPageStarted(WebView view, String url, Bitmap favicon) {
           mainProgressBar.setVisibility(View.VISIBLE);
           current_page_url=url;
           super.onPageStarted(view, url, favicon);
           invalidateOptionsMenu();
       }

   }

    private void onClickEventBottomView() {
            tvBookmark.setOnClickListener(view -> {
                String message;
                SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String jsonLink = sharedPreferences.getString(WEB_LINKS, null);

                if (jsonLink != null) {

                    Gson gson = new Gson();
                    ArrayList<String> linkList = gson.fromJson(jsonLink, new TypeToken<ArrayList<String>>() {
                    }.getType());
                    if (linkList.contains(current_page_url)) {
                        linkList.remove(current_page_url);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                        editor.apply();
                        tvBookmark.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bookmark_white_24dp, 0, 0);
                        message = "Bookmark Removed";

                    } else {
                        linkList.add(current_page_url);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                        editor.apply();
                        tvBookmark.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_bookmarks_black_24dp, 0, 0);
                        message = "Bookmarked";
                    }
                } else {
                ArrayList<String> linkList = new ArrayList<>();
                linkList.add(current_page_url);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                editor.apply();
                tvBookmark.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_bookmarks_black_24dp, 0, 0);
                message = "Bookmarked";

                }
                Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
                snackbar.show();
                invalidateOptionsMenu();
            });


        tvBack.setOnClickListener(view -> {
            if (doubleBackToExitPressedOnce) {
                onBackPressed();
            }
            MainActivity.this.doubleBackToExitPressedOnce = true;
            Toast.makeText(mContext, R.string.back_toast_message,Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.menu_nav_item, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_bookmarks:
                Intent intent = new Intent(MainActivity.this,SavedBookmark.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        /*super.onBackPressed();*/
        if (doubleBackToExitPressedOnce) {
            onBackPressed();
        }
        MainActivity.this.doubleBackToExitPressedOnce = true;
        Toast.makeText(mContext, R.string.back_toast_message,Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }


}
