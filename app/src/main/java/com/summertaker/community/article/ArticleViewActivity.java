package com.summertaker.community.article;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.github.bluzwong.swipeback.SwipeBackActivityHelper;
import com.squareup.picasso.Picasso;
import com.summertaker.community.R;
import com.summertaker.community.common.BaseActivity;
import com.summertaker.community.common.BaseApplication;
import com.summertaker.community.common.ImageViewActivity;
import com.summertaker.community.data.ArticleDetailData;
import com.summertaker.community.data.CommentData;
import com.summertaker.community.parser.RuliwebParser;
import com.summertaker.community.parser.TheqooParser;
import com.summertaker.community.parser.TodayhumorParser;
import com.summertaker.community.util.ExpandableHeightListView;
import com.summertaker.community.util.ProportionalImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArticleViewActivity extends BaseActivity implements ArticleViewInterface {

    private String mTitle;
    private String mUrl = "";

    private ProgressBar mProgressBar;
    private ScrollView mScrollView;

    private LinearLayout.LayoutParams mParams;
    private LinearLayout.LayoutParams mParamsNoMargin;

    private ArticleDetailData mArticleDetailData;
    private ArrayList<CommentData> mCommentDataList;

    SwipeBackActivityHelper helper = new SwipeBackActivityHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_view_activity);

        Intent intent = getIntent();
        mTitle = intent.getStringExtra("title");
        mUrl = intent.getStringExtra("url");

        setSwipeDetector(); // 스와이프 종료
        setBaseStatusBar(); // 상태바 설정
        setBaseToolbar();   // 툴바 설정

        mProgressBar = findViewById(R.id.toolbar_progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        mScrollView = findViewById(R.id.scrollView);

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(mTitle);

        /*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                finish();
            }
        });
        */

        float density = getResources().getDisplayMetrics().density;
        int height = (int) (272 * density);
        int margin = (int) (16 * density);
        mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        mParams.setMargins(0, margin, 0, 0); // Ctrl + MouseOver
        mParamsNoMargin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);

        requestData(mUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_open_in_new:
                openInNew();
                return true;
            case R.id.action_share:
                share();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestData(final String url) {
        //Log.e(mTag, url);

        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(mTag, response.toString());
                parseData(url, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                parseData(url, "");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", BaseApplication.getInstance().getMobileUserAgent());
                return headers;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, mVolleyTag);
    }

    private void parseData(String url, String response) {
        if (url.contains("theqoo")) {
            TheqooParser theqooParser = new TheqooParser();
            mArticleDetailData = theqooParser.parseDetail(response);
            renderData();
        } else if (url.contains("todayhumor")) {
            TodayhumorParser todayhumorParser = new TodayhumorParser();
            if (url.contains("ajax_memo_list.php")) {
                mCommentDataList = todayhumorParser.parseComment(response);
                renderComment();
            } else {
                mArticleDetailData = todayhumorParser.parseDetail(response);
                renderData();
            }
        } else if (url.contains("ruliweb")) {
            RuliwebParser ruliwebParser = new RuliwebParser();
            mArticleDetailData = ruliwebParser.parseDetail(response);
            renderData();
        }
    }

    private void renderData() {
        //Log.d(mTag, "==========================");
        //Log.d(mTag, mContent);

        int imageCount = 0;

        if (mArticleDetailData.getThumbnails() != null) {
            imageCount = mArticleDetailData.getThumbnails().size();

            if (imageCount == 1) {
                //------------------------
                // 사진이 한 개인 경우
                //------------------------
                final String thumbnail = mArticleDetailData.getThumbnails().get(0);
                final String image = mArticleDetailData.getImages().get(0);

                if (!thumbnail.isEmpty()) {
                    ImageView iv = findViewById(R.id.ivPicture);
                    iv.setVisibility(View.VISIBLE);

                    if (thumbnail.toLowerCase().contains(".gif")) {
                        Glide.with(this).asGif().load(thumbnail).into(iv);
                    } else {
                        Picasso.with(this).load(thumbnail).placeholder(R.drawable.placeholder).into(iv);
                    }

                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Log.e(">>", imageUrl);
                            if (image != null && !image.isEmpty()) {
                                viewImage(image);
                            }
                        }
                    });

                    // ERROR: Bitmap too large to be uploaded into a texture (720x15207, max=8192x8192)

                    /*Picasso.with(this).load(thumbnail).placeholder(R.drawable.placeholder).into(new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            int width = bitmap.getWidth();
                            Log.d(mTag, "============= size: " + width + "x " + bitmap.getHeight());
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Log.e(mTag, "IMAGE SAVE ERROR!!! onBitmapFailed()");
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });*/
                }
            } else if (imageCount > 1) {
                //------------------------
                // 사진이 여러 개인 경우
                //------------------------
                LinearLayout loPicture = findViewById(R.id.loPicture);
                //loPicture.removeAllViews();
                loPicture.setVisibility(View.VISIBLE);

                for (int i = 0; i < imageCount; i++) {
                    final String thumbnail = mArticleDetailData.getThumbnails().get(i);
                    final String image = mArticleDetailData.getImages().get(i);
                    if (thumbnail.isEmpty()) {
                        continue;
                    }

                    final ProportionalImageView iv = new ProportionalImageView(this);
                    //final ImageView iv = new ImageView(this);
                    //if (i == imageArray.length - 1) {
                    if (i == 0) {
                        iv.setLayoutParams(mParamsNoMargin);
                    } else {
                        iv.setLayoutParams(mParams);
                    }
                    //iv.setAdjustViewBounds(true);
                    //iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    //iv.setBackgroundColor(getResources().getColor(R.color.card_background));
                    loPicture.addView(iv);

                    //int placeholder = R.drawable.placeholder_green;
                    //if (thumbnailUrl.contains("nogizaka46")) {
                    //    placeholder = R.drawable.placeholder_purple;
                    //}

                    if (thumbnail.toLowerCase().contains(".gif")) {
                        Glide.with(this).asGif().load(thumbnail).into(iv);
                    } else {
                        Picasso.with(this).load(thumbnail).placeholder(R.drawable.placeholder).into(iv);
                    }
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Log.e(">>", imageUrl);
                            if (image != null && !image.isEmpty()) {
                                viewImage(image);
                            }
                        }
                    });
                }
            }
        }

        // 내용 텍스트 출력하기
        String content = mArticleDetailData.getContent();
        if (!content.isEmpty()) {
            if (imageCount > 0) {
                // 사진과 내용 사이에 공간 만들기
                View view = findViewById(R.id.vwImageSpace);
                view.setVisibility(View.VISIBLE);
            }

            TextView tvContent = findViewById(R.id.tvContent);
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(content);
        }

        if (mUrl.contains("todayhumor")) {
            // 댓글 로드하기
            loadComment();
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadComment() {
        String url = "http://m.todayhumor.co.kr/ajax_memo_list.php?parent_table="
                + mArticleDetailData.getParentTable() + "&parent_id="
                + mArticleDetailData.getParentId() + "&is_mobile=Y";
        requestData(url);
    }

    private void renderComment() {
        ArticleCommentAdapter commentAdapter = new ArticleCommentAdapter(this, mCommentDataList, this);

        if (mCommentDataList.size() > 0) {
            LinearLayout loCommentHeader = findViewById(R.id.loCommentHeader);
            loCommentHeader.setVisibility(View.VISIBLE);

            ExpandableHeightListView expandableListView = findViewById(R.id.listView);
            expandableListView.setVisibility(View.VISIBLE);
            expandableListView.setAdapter(commentAdapter);
            expandableListView.setExpanded(true);
        }

        mProgressBar.setVisibility(View.GONE);
    }

    public void goTop() {
        mScrollView.fullScroll(View.FOCUS_UP);
    }

    public void openInNew() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
        startActivity(intent);
    }

    public void share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mUrl);
        startActivity(Intent.createChooser(shareIntent, mTitle));
    }

    @Override
    public void onPictureClick(int position) {
        CommentData data = mCommentDataList.get(position);
        viewImage(data.getImage());
    }

    @Override
    public void onContentClick(int position) {

    }

    private void viewImage(String url) {
        if (url.contains("theqoo")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ImageViewActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);
        }
    }
}
