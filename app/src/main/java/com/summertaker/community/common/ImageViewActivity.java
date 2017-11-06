package com.summertaker.community.common;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.summertaker.community.R;

public class ImageViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_activity);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        //Log.d(mTag, url);

        //setSwipeDetector(); // 스와이프 종료
        setBaseStatusBar(); // 상태바 설정
        setBaseToolbar();   // 툴바 설정

        /*
        mWebView = findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClientClass());
        //mWebView.loadUrl(mUrl);

        String img = "<img src=\"" + mUrl + "\">";
        String webData =  "<!DOCTYPE html>"
                + "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><title></title></head>"
                + "<body style=\"padding:0;margin:0;border:0;\">" + img + "</body>"
                + "</html>";
        mWebView.loadData(webData, "text/html", "UTF-8");
        */

        // https://github.com/jsibbold/zoomage
        ImageView ivPicture = findViewById(R.id.ivPicture);
        Glide.with(this).load(url).into(ivPicture);
        //Picasso.with(this).load(url).into(ivPicture);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //mCallback.onWebFragmentEvent("onPageStarted", mWebView.getUrl(), mWebView.canGoBack());
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //mCallback.onWebFragmentEvent("onPageFinished", mWebView.getUrl(), mWebView.canGoBack());
        }
    }
}
