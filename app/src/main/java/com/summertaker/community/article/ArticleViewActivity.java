package com.summertaker.community.article;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.squareup.picasso.Picasso;
import com.summertaker.community.R;
import com.summertaker.community.common.BaseActivity;
import com.summertaker.community.common.BaseApplication;
import com.summertaker.community.common.BaseParser;
import com.summertaker.community.common.ImageViewActivity;
import com.summertaker.community.data.ArticleDetailData;
import com.summertaker.community.data.CommentData;
import com.summertaker.community.data.MediaData;
import com.summertaker.community.parser.BobaedreamParser;
import com.summertaker.community.parser.ClienParser;
import com.summertaker.community.parser.InstagramParser;
import com.summertaker.community.parser.MlbparkParser;
import com.summertaker.community.parser.PpomppuParser;
import com.summertaker.community.parser.RuliwebParser;
import com.summertaker.community.parser.TheqooParser;
import com.summertaker.community.parser.TodayhumorParser;
import com.summertaker.community.parser.TwitterParser;
import com.summertaker.community.util.ExpandableHeightListView;
import com.summertaker.community.util.ImageUtil;
import com.summertaker.community.util.ProportionalImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleViewActivity extends BaseActivity implements ArticleViewInterface {

    private String mTitle;
    private String mUrl;

    private ProgressBar mProgressBar;
    private ScrollView mScrollView;

    private LinearLayout mLoPicture;
    private int mMediaCount = 0;

    private LinearLayout.LayoutParams mPictureParams;
    private LinearLayout.LayoutParams mPictureParamsNoMargin;
    private RelativeLayout.LayoutParams mRelativeParams;
    private RelativeLayout.LayoutParams mRelativeParamsNoMargin;
    private LinearLayout.LayoutParams mTextParams;
    private RelativeLayout.LayoutParams mIconParams;

    private ArticleDetailData mArticleDetailData = new ArticleDetailData();
    private ArrayList<CommentData> mCommentDataList = new ArrayList<>();

    private String document_srl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_view_activity);

        // Init the swipe back
        SwipeBack.attach(this, Position.LEFT)
                .setContentView(R.layout.article_view_activity)
                .setSwipeBackView(R.layout.swipeback_custom);

        Intent intent = getIntent();
        String section = intent.getStringExtra("section");
        mTitle = intent.getStringExtra("title");
        mUrl = intent.getStringExtra("url");
        //Log.e(mTag, "mUrl: " + mUrl);

        //http://theqoo.net/index.php?mid=jdol&filter_mode=normal&category=29770&document_srl=600062317
        String regex = "&document_srl=\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(mUrl);
        while (match.find()) {
            document_srl = match.group();
            //Log.e(mTag, "document_srl: " + document_srl);
        }

        //setSwipeDetector(); // 스와이프 종료
        setBaseStatusBar(); // 상태바 설정
        setBaseToolbar(section); // 툴바 설정

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
        mTextParams.setMargins(0, margin, 0, 0);
        mIconParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //mIconParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        doStringRequest(mUrl, Request.Method.GET);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.swipeback_stack_to_front, R.anim.swipeback_stack_right_out);
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

    private void doStringRequest(final String url, int method) {
        //Log.e(mTag, url);

        StringRequest stringRequest = new StringRequest(method, url, new Response.Listener<String>() {
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
                //SharedPreferences _preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                //String sessionId = _preferences.getString("sessionid", "");

                HashMap<String, String> headers = new HashMap<>();

                //headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", BaseApplication.getInstance().getMobileUserAgent());

                //StringBuilder builder = new StringBuilder();
                //builder.append("sessionid");
                //builder.append("=");
                //builder.append(sessionId);
                //if (headers.containsKey("Cookie")) {
                //    builder.append("; ");
                //    builder.append(headers.get("Cookie"));
                //}
                headers.put("cmtimg", "1");

                return headers;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(stringRequest, mVolleyTag);
    }

    private void doJsonObjectRequest(final String url) {
        HashMap<String, String> params = new HashMap<>();
        params.put("act", "dispBoardContentCommentListTheqoo");
        params.put("document_srl", document_srl);
        params.put("cpage", "0");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    VolleyLog.e("Response:%n %s", response.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("JsonObjectRequest.onErrorResponse(): ", error.getMessage());
            }
        });

        BaseApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void parseData(String url, String response) {
        if ("http://theqoo.net/index.php".equals(url)) {
            // 더쿠 댓글
            Log.e(mTag, response);
        } else if (url.contains("theqoo")) {
            // 더쿠 글 내용
            TheqooParser theqooParser = new TheqooParser();
            mArticleDetailData = theqooParser.parseDetail(response);
            renderData();
        } else if (url.contains("todayhumor")) {
            // 오유 글 내용
            TodayhumorParser todayhumorParser = new TodayhumorParser();
            if (url.contains("ajax_memo_list.php")) {
                mCommentDataList = todayhumorParser.parseComment(response);
                renderComment();
            } else {
                mArticleDetailData = todayhumorParser.parseDetail(response);
                renderData();
            }
        } else if (url.contains("ruliweb")) {
            // 루리웹 글 내용, 댓글
            RuliwebParser ruliwebParser = new RuliwebParser();
            ruliwebParser.parseDetail(response, mArticleDetailData, mCommentDataList);
            renderData();
        } else if (url.contains("bobaedream")) {
            // 보배드림 글 내용
            BobaedreamParser bobaedreamParser = new BobaedreamParser();
            bobaedreamParser.parseDetail(response, mArticleDetailData, mCommentDataList);
            renderData();
        } else if (url.contains("mlbpark")) {
            // 엠팍 글 내용
            MlbparkParser mlbparkParser = new MlbparkParser();
            mlbparkParser.parseDetail(response, mArticleDetailData);
            renderData();
        } else if (url.contains("clien")) {
            // 클리앙 글 내용
            ClienParser clienParser = new ClienParser();
            if (url.contains("/comment")) {
                mCommentDataList = clienParser.parseComment(response);
                renderComment();
            } else {
                clienParser.parseDetail(response, mArticleDetailData);
                renderData();
            }
        } else if (url.contains("ppomppu")) {
            // 뽐뿌 글 내용
            PpomppuParser ppomppuParser = new PpomppuParser();
            mArticleDetailData = ppomppuParser.parseDetail(response);
            renderData();
        }
    }

    private void renderData() {
        //Log.e(mTag, "renderData()...");

        mLoPicture = findViewById(R.id.loPicture);

        if (mArticleDetailData.getMediaDatas() != null) {
            int mediaTotal = mArticleDetailData.getMediaDatas().size();
            //Log.e(mTag, "mediaTotal: " + mediaTotal);

            for (MediaData mediaData : mArticleDetailData.getMediaDatas()) {
                crateMediaData(mediaData);

                if (mMediaCount >= 20 && mMediaCount < mediaTotal) { // 스마트폰 메모리 부족 에러 발생한다.
                    TextView tvPicture = findViewById(R.id.tvPicture);
                    tvPicture.setVisibility(View.VISIBLE);

                    String text = "( 사진 " + mMediaCount + "개 출력 / 전체 " + mediaTotal + "개 )";
                    tvPicture.setText(text);

                    break;
                }
            }
        }

        /*
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

                            //crateImage(thumbnail, outLink);
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
        */

        if (mMediaCount > 0) {
            mLoPicture.setVisibility(View.VISIBLE);
        }

        // 내용 텍스트 출력하기
        String content = mArticleDetailData.getContent();
        if (content != null && !content.isEmpty()) {
            if (mMediaCount > 0) {
                // 사진과 내용 사이에 공간 만들기
                View view = findViewById(R.id.vwImageSpace);
                view.setVisibility(View.VISIBLE);
            }

            // 출처 출력하기
            final String source = mArticleDetailData.getSource();
            if (source != null && !source.isEmpty()) {
                TextView tvSource = findViewById(R.id.tvSource);
                tvSource.setVisibility(View.VISIBLE);
                int maxLen = 36;
                String sourceHtml = (source.length() > maxLen) ? source.substring(0, maxLen - 3) + "..." : source;
                sourceHtml = "출처: <a href=\"" + source + "\">" + sourceHtml + "</a>";
                tvSource.setText(Html.fromHtml(sourceHtml));
                tvSource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openInNew(source);
                    }
                });
            }

            TextView tvContent = findViewById(R.id.tvContent);
            tvContent.setVisibility(View.VISIBLE);

            if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                //HtmlTextView htmlTextView = findViewById(R.id.html_text_view);
                //htmlTextView.setHtml(content, new HtmlHttpImageGetter(htmlTextView));

                // https://medium.com/@rajeefmk/android-textview-and-image-loading-from-url-part-1-a7457846abb6
                Spannable html = ImageUtil.getSpannableHtmlWithImageGetter(ArticleViewActivity.this, tvContent, content);
                ImageUtil.setClickListenerOnHtmlImageGetter(html, new ImageUtil.Callback() {
                    @Override
                    public void onImageClick(String imageUrl) {
                        //Log.e(mTag, "imageUrl: " + imageUrl);
                        viewImage(imageUrl);
                    }
                }, true);
                tvContent.setText(html);
                tvContent.setMovementMethod(LinkMovementMethod.getInstance()); // URL 클릭 시 이동
            } else {
                tvContent.setText(content);
            }
        }

        // 댓글 로드하기
        if (mUrl.contains("todayhumor")) {
            // 오유 댓글
            String url = "http://m.todayhumor.co.kr/ajax_memo_list.php?parent_table="
                    + mArticleDetailData.getTable() + "&parent_id="
                    + mArticleDetailData.getId() + "&is_mobile=Y";
            doStringRequest(url, Request.Method.GET);
        } else if (mUrl.contains("ruliweb")) {
            // 루리웹 댓글
            renderComment();
        } else if (mUrl.contains("bobaedream")) {
            // 보배드림 댓글
            renderComment();
        } else if (mUrl.contains("clien")) {
            // 클리앙 댓글
            // https://m.clien.net/service/board/park/11501977?po=0&od=T33&sk=&sv=&category=&groupCd=clien_all&articlePeriod=default
            String[] array = mUrl.split("\\?");
            String url = array[0].replace("/service/", "/service/api/");
            url = url + "/comment?param={\"order\":\"date\",\"po\":0,\"ps\":100}";
            Log.e(mTag, "comment url: " + url);
            doStringRequest(url, Request.Method.GET);
        } else if (mUrl.contains("theqoo")) {
            // 더쿠 댓글
            //String url = "http://theqoo.net/index.php"; //?act=dispBoardContentCommentListTheqoo&document_srl=" + document_srl + "&cpage=0";
            //doJsonObjectRequest(url);
            mProgressBar.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * ImageView 만들어서 이미지, 동영상 썸네일 출력하기
     */
    private void crateMediaData(final MediaData mediaData) {
        final String thumbnail = mediaData.getThumbnail();
        if (thumbnail == null || thumbnail.isEmpty()) {
            return;
        }
        Log.e(mTag, "crateMediaData.thumbnail: " + thumbnail);

        final ProportionalImageView iv = new ProportionalImageView(this);

        if (thumbnail.contains("youtube.com")) {
            //--------------------------------------------------
            // 동영상은 썸네일 위에 플레이 아이콘을 표시한다.
            //--------------------------------------------------
            Log.e(mTag, "youtube...");

            final RelativeLayout rl = new RelativeLayout(this);
            if (mMediaCount == 0) {
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
            //} else if (thumbnail.contains("twitter.com")) {
            // 트위터
        } else {
            // 일반 썸네일을 위한 이미지뷰 추가
            if (mMediaCount == 0) {
                iv.setLayoutParams(mPictureParamsNoMargin);
            } else {
                iv.setLayoutParams(mPictureParams);
            }
            mLoPicture.addView(iv);
        }

        iv.setBackgroundResource(R.drawable.placeholder);

        if (thumbnail.contains("twitter.com") || thumbnail.contains("instagram.com")) {
            //-----------------------
            // 트위터/인스타그램 사진 가져오기
            //-----------------------
            //Log.e(mTag, "Request: " + thumbnail);

            Glide.with(getApplicationContext()).asGif().load(R.drawable.loading).into(iv);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, thumbnail, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Log.e(mTag, "response: " + response);

                    BaseParser parser = null;
                    if (thumbnail.contains("twitter.com")) {
                        parser = new TwitterParser();
                    } else if (thumbnail.contains("instagram.com")) {
                        parser = new InstagramParser();
                    }

                    if (parser != null) {
                        MediaData md = parser.getMediaData(response);
                        String img = md.getThumbnail();
                        //Log.e(mTag, "img: " + img);

                        if (img == null || img.isEmpty()) {
                            // 실제 사진이 없는 경우 (트위터에 업로드된 사진이 없거나 블로그 등의 위부 이미지를 트윗한 경우)
                            mLoPicture.removeView(iv);
                        } else if (mediaData.getUrl().contains("twitter.com")) {
                            // 트위터
                            iv.setVisibility(View.GONE);
                        } else {
                            Picasso.with(getApplicationContext()).load(img).into(iv, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    //Log.e(mTag, "Piccaso.onSuccess(): " + thumbnail);
                                }

                                @Override
                                public void onError() {
                                    //Log.e(mTag, "Piccaso.onError(): " + thumbnail);
                                    Toast.makeText(getApplicationContext(), "Piccaso.onError()...", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        // 내용 만들기
                        TextView tv = new TextView(getApplicationContext());
                        tv.setLayoutParams(mTextParams);
                        tv.setTextColor(Color.BLACK);
                        mLoPicture.addView(tv);

                        Spannable html = ImageUtil.getSpannableHtmlWithImageGetter(ArticleViewActivity.this, tv, md.getHtml());
                        //ImageUtil.setClickListenerOnHtmlImageGetter(html, new ImageUtil.Callback() {
                        //    @Override
                        //    public void onImageClick(String imageUrl) {
                        //        //Log.e(mTag, "imageUrl: " + imageUrl);
                        //        viewImage(imageUrl);
                        //    }
                        //}, true);
                        tv.setText(html);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());

                        // 내용 만들기
                        //TextView tvDescription = new TextView(getApplicationContext());
                        //tvDescription.setLayoutParams(mTextParams);
                        //tvDescription.setTextColor(Color.BLACK);
                        //mLoPicture.addView(tvDescription);
                        //tvDescription.setText(md.getDescription());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(mTag, "stringRequest.onErrorResponse(): " + error.getLocalizedMessage());
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
            BaseApplication.getInstance().addToRequestQueue(stringRequest, mVolleyTag);
        } else {
            Glide.with(this).asGif().load(R.drawable.loading).into(iv);
            if (thumbnail.toLowerCase().contains(".gif")) {
                Glide.with(this).asGif().load(thumbnail).listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        //Log.e(mTag, "Glide.onLoadFailed(): " + thumbnail);
                        String msg = "Glide.onLoadFailed()...";
                        if (e != null) {
                            msg += "\n" + e.getLocalizedMessage();
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        return false;
                    }
                })/*.apply(new RequestOptions().placeholder(R.drawable.placeholder))*/.into(iv);
            } else {
                Glide.with(this).load(thumbnail)/*.apply(new RequestOptions().placeholder(R.drawable.placeholder))*/.into(iv);
            }
        }

        // 썸네일 클릭 이벤트 처리하기
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mediaData.getUrl();
                String image = mediaData.getImage();

                if (url != null && !url.isEmpty()) {
                    openInNew(url);
                } else if (image != null && !image.isEmpty()) {
                    viewImage(image);
                }
            }
        });

        mMediaCount++;
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
        String emails[] = {"summertaker@gmail.com"};

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, emails);
        intent.putExtra(Intent.EXTRA_SUBJECT, mTitle);
        intent.putExtra(Intent.EXTRA_TEXT, mUrl);
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
    }

    @Override
    public void onPictureClick(int position) {
        CommentData data = mCommentDataList.get(position);
        viewImage(data.getUrl());
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
