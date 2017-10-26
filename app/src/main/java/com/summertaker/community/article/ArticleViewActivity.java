package com.summertaker.community.article;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArticleViewActivity extends BaseActivity implements ArticleViewInterface {

    private String mTitle;
    private String mUrl = "";

    private ProgressBar mProgressBar;
    private ScrollView mScrollView;

    private LinearLayout mLoPicture;
    private int mImageCount = 0;

    private LinearLayout.LayoutParams mPictureParams;
    private LinearLayout.LayoutParams mPictureParamsNoMargin;
    private RelativeLayout.LayoutParams mRelativeParams;
    private RelativeLayout.LayoutParams mRelativeParamsNoMargin;
    private LinearLayout.LayoutParams mTextParams;
    private RelativeLayout.LayoutParams mIconParams;

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
        setBaseToolbar(mTitle);   // 툴바 설정

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
        mPictureParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        mPictureParams.setMargins(0, margin, 0, 0); // Ctrl + MouseOver
        mPictureParamsNoMargin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);

        mRelativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mRelativeParams.setMargins(0, margin, 0, 0); // Ctrl + MouseOver
        mRelativeParamsNoMargin = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        mTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mIconParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //mIconParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

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
                openInNew(mUrl);
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

        mLoPicture = findViewById(R.id.loPicture);

        int imageTotal = 0;

        if (mArticleDetailData.getThumbnails() != null) {
            imageTotal = mArticleDetailData.getThumbnails().size();
            if (imageTotal > 0) {
                for (int i = 0; i < imageTotal; i++) {
                    final String thumbnail = mArticleDetailData.getThumbnails().get(i);
                    //Log.e(mTag, "thumbnail: " + thumbnail);
                    final String image = mArticleDetailData.getImages().get(i);
                    //Log.e(mTag, "image: " + image);

                    if (thumbnail.isEmpty()) {
                        continue;
                    }
                    crateImage(thumbnail, image, null);

                    if (mImageCount >= 20 && mImageCount < imageTotal) { // 스마트폰 메모리 부족 에러 발생한다.
                        TextView tvPicture = findViewById(R.id.tvPicture);
                        tvPicture.setVisibility(View.VISIBLE);
                        String text = "( 사진 " + mImageCount + "개 출력 / 전체 " + imageTotal + "개 )";
                        tvPicture.setText(text);
                        break;
                    }
                }
            }
        }

        //-------------------------------
        // 외부 링크 URL 처리하기
        //-------------------------------
        int outLinkCount = 0;
        if (mArticleDetailData.getOutLinks() != null) {
            outLinkCount = mArticleDetailData.getOutLinks().size();

            if (outLinkCount > 0) {
                LinearLayout loOutLink = findViewById(R.id.loOutlink);
                loOutLink.setVisibility(View.VISIBLE);

                for (int i = 0; i < outLinkCount; i++) {
                    final String outLink = mArticleDetailData.getOutLinks().get(i);
                    //Log.e(mTag, "outLink: " + outLink);

                    if (outLink.contains("youtube.com") || outLink.contains("instagram.com")) {
                        if (outLink.contains("youtube.com")) {
                            //--------------------------------------------------------------
                            // 유튜브 링크(URL 또는 IFRAME)는 동영상 썸네일 출력
                            // https://img.youtube.com/vi/YVVF29kRCLI/0.jpg
                            // https://www.youtube.com/embed/kwr4dEZp8kc?list=PLt6W_jGRdpx_dFH3VmL74s9BDUZjt0tI1
                            //--------------------------------------------------------------
                            String url = outLink.split("\\?")[0];
                            String id = url.replace("https://www.youtube.com/embed/", "");
                            final String thumbnail = "https://img.youtube.com/vi/" + id + "/0.jpg";
                            //Log.e(mTag, "thumbnail: " + thumbnail);
                            crateImage(thumbnail, null, outLink);
                        } else if (outLink.contains("instagram.com")) {
                            //------------------------------
                            // 인스타그램 이미지
                            //------------------------------
                            //loadOutlinkImage(outLink, iv);
                        }
                    } else {
                        //-----------------------------------------------
                        // 일반적인 외부 링크는 클릭할 수 있도록 URL 출력
                        //-----------------------------------------------
                        TextView tv = new TextView(this);
                        tv.setLayoutParams(mTextParams);
                        loOutLink.addView(tv);
                        tv.setText(outLink);
                        tv.setTextColor(getResources().getColor(R.color.hyperLink));

                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.e(mTag, outLink);
                                openInNew(outLink);
                            }
                        });
                    }
                }
            }
        }

        if (mImageCount > 0) {
            mLoPicture.setVisibility(View.VISIBLE);
        }

        // 내용 텍스트 출력하기
        String content = mArticleDetailData.getContent();
        if (!content.isEmpty()) {
            if (imageTotal > 0 || outLinkCount > 0) {
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

    /**
     * ImageView 만들어서 이미지 출력하기
     */
    private void crateImage(final String thumbnail, final String image, final String url) {
        final ProportionalImageView iv = new ProportionalImageView(this);
        if (thumbnail.contains("youtube.com")) {
            //--------------------------------------------------
            // 동영상은 썸네일 위에 플레이 아이콘을 표시한다.
            //--------------------------------------------------
            final RelativeLayout rl = new RelativeLayout(this);
            if (mImageCount == 0) {
                rl.setLayoutParams(mRelativeParamsNoMargin);
            } else {
                rl.setLayoutParams(mRelativeParams);
            }
            rl.setGravity(Gravity.CENTER);

            iv.setLayoutParams(mPictureParamsNoMargin);

            ImageView ivIcon = new ImageView(this); // 플레이 아이콘 표시하기
            ivIcon.setBackgroundResource(R.drawable.youtube64);
            ivIcon.setLayoutParams(mIconParams);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivIcon.getLayoutParams(); // 아이콘 가운데로 맞추기
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            ivIcon.setLayoutParams(layoutParams);

            rl.addView(iv);
            rl.addView(ivIcon);
            mLoPicture.addView(rl);
        } else {
            if (mImageCount == 0) {
                iv.setLayoutParams(mPictureParamsNoMargin);
            } else {
                iv.setLayoutParams(mPictureParams);
            }
            mLoPicture.addView(iv);
        }

        if (thumbnail.toLowerCase().contains(".gif")) {
            Glide.with(this).asGif().load(thumbnail).into(iv);
        } else {
            //Glide.with(this).load(thumbnail).into(iv);
            Picasso.with(this).load(thumbnail).placeholder(R.drawable.placeholder).into(iv, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    //Log.e(mTag, "Piccaso.onSuccess(): " + thumbnail);
                }

                @Override
                public void onError() {
                    Log.e(mTag, "Piccaso.onError(): " + thumbnail);
                }
            });
        }

        if (image != null && !image.isEmpty()) {
            // 원본 이미지가 있는 경우
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewImage(image);
                }
            });
        } else if (url != null && !url.isEmpty()) {
            // 링크 페이지가 있는 경우
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openInNew(url);
                }
            });
        }

        mImageCount++;
    }

    /**
     * 외부 사이트 페이지의 대표 이미지 로드하기
     */
    private void loadOutlinkImage(final String url, final ImageView iv) {
        Log.e(mTag, "loadOutlinkImage(): " + url);

        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(mTag, response.toString());
                //parseData(url, response);
                Document doc = Jsoup.parse(response);
                if (url.contains("youtube.com")) {
                    Log.e(mTag, response);
                } else {
                    Element root = doc.select(".xe_content").first();
                }
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

    public void openInNew(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
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
