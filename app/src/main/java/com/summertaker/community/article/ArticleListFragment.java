package com.summertaker.community.article;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.community.R;
import com.summertaker.community.common.BaseApplication;
import com.summertaker.community.data.ArticleListData;
import com.summertaker.community.data.SiteData;
import com.summertaker.community.parser.ClienParser;
import com.summertaker.community.parser.PpomppuParser;
import com.summertaker.community.parser.RuliwebParser;
import com.summertaker.community.parser.TheqooParser;
import com.summertaker.community.parser.TodayhumorParser;
import com.summertaker.community.util.EndlessScrollListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArticleListFragment extends Fragment implements ArticleListInterface {

    private Context mContext;
    private String mTag = "== " + this.getClass().getSimpleName();
    private String mVolleyTag = this.getClass().getSimpleName();

    private ArticleListFragmentListener mListener;

    private SiteData mSiteData;
    private int mCurrentPage = 1;
    private boolean mIsLoading = false;
    private boolean mIsDataExists = true;

    private ArrayList<ArticleListData> mArticleList;
    private ArticleListAdapter mAdapter;
    private ListView mListView;
    private EndlessScrollListener mEndlessScrollListener;
    private LinearLayout mLoLoadMore;

    private boolean mIsReloadMode = false;

    // Container Activity must implement this interface
    public interface ArticleListFragmentListener {
        public void onArticleListFragmentEvent(String event);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mListener = (ArticleListFragmentListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
            }
        }
    }

    public ArticleListFragment() {
    }

    public static ArticleListFragment newInstance(int position) {
        ArticleListFragment fragment = new ArticleListFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        //args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.article_list_fragment, container, false);

        //mLoLoading = rootView.findViewById(R.id.loLoading);
        //mPbLoading = rootView.findViewById(R.id.pbLoading);
        //mLoLoadMore = rootView.findViewById(R.id.loLoadMore);

        mContext = getContext().getApplicationContext();

        mArticleList = new ArrayList<>();
        mAdapter = new ArticleListAdapter(getContext(), mArticleList);

        mListView = rootView.findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                ArticleListData data = (ArticleListData) adapterView.getItemAtPosition(pos);

                String section = mSiteData.getTitle();
                String title = data.getTitle();
                String url = data.getUrl();

                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //startActivity(intent);

                //Intent intent = new Intent(getActivity(), WebViewActivity.class);
                //Intent intent = new Intent(getActivity(), WebActivity.class);

                //-------------------------------------------------------------------------
                // "Empty Activity" 템플릿 사용 시 툴바에 프로그레스바 표시할 때 사용하는
                // setSupportProgressBarIndeterminateVisibility(true);가 Deprecated 됨
                //-------------------------------------------------------------------------
                //Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
                // 그래서 "Basic Activity" 템플릿 사용해서 직접 프로그레스바를 추가함
                // https://stackoverflow.com/questions/27788195/setprogressbarindeterminatevisibilitytrue-not-working
                Intent intent = new Intent(getActivity(), ArticleViewActivity.class);
                //Intent intent = new Intent(getActivity(), ArticleActivity.class);
                intent.putExtra("section", section);
                intent.putExtra("title", title);
                intent.putExtra("url", url);

                startActivity(intent);
            }
        });

        // mListView.setLongClickable(true); // in XML
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                //Log.e("long clicked","pos: " + pos);

                ArticleListData data = (ArticleListData) adapterView.getItemAtPosition(pos);
                String title = data.getTitle();
                String url = data.getUrl();

                //Intent shareIntent = new Intent(Intent.ACTION_SEND);
                //shareIntent.setType("text/plain");
                //shareIntent.putExtra(Intent.EXTRA_TEXT, url);
                //startActivity(Intent.createChooser(shareIntent, title));

                Intent shareIntent = new Intent();
                shareIntent.setType("text/plain");
                shareIntent.setPackage("com.ideashower.readitlater");
                shareIntent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(Intent.createChooser(shareIntent, title));

                return true;
            }
        });

        mEndlessScrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (mIsDataExists) {
                    //Log.e(mTag, "page: " + page);
                    loadData();
                    return true; // ONLY if more data is actually being loaded; false otherwise.
                } else {
                    return false;
                }
            }
        };

        mListView.setOnScrollListener(mEndlessScrollListener);

        mLoLoadMore = rootView.findViewById(R.id.loLoadMore);

        int position = getArguments().getInt("position");

        mSiteData = BaseApplication.getInstance().getSiteList().get(position);
        //mUserAgent = mSiteData.getUserAgent();
        //mRequestUrl = mSiteData.getUrl();

        //if (mSiteData.getUrl().contains("clien")) {
        //    mCurrentPage = 0;
        //}

        loadData();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 데이터 로드하기
     */
    private void loadData() {
        if (!mIsLoading) {
            mIsLoading = true;
            mListener.onArticleListFragmentEvent("onLoadDataStarted");

            mLoLoadMore.setVisibility(View.VISIBLE);

            requestData();
        }
    }

    private void requestData() {
        String url = mSiteData.getUrl();

        if (mCurrentPage > 1) {
            int page = mCurrentPage;
            if (url.contains("clien")) {
                page = page - 1;
            }
            url += mSiteData.getPageParam() + page;
            //mLoLoadMore.setVisibility(View.VISIBLE);
        }
        //Log.e(mTag, "url: " + url);

        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(mTag, response.toString());
                parseData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                parseData("");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", mSiteData.getUserAgent());
                return headers;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, mVolleyTag);
    }

    private void parseData(String response) {
        if (mSiteData.getUrl().contains("theqoo")) {
            TheqooParser theqooParser = new TheqooParser();
            theqooParser.parseList(response, mArticleList);
        } else if (mSiteData.getUrl().contains("todayhumor")) {
            TodayhumorParser todayhumorParser = new TodayhumorParser();
            todayhumorParser.parseList(response, mArticleList);
        } else if (mSiteData.getUrl().contains("ruliweb")) {
            RuliwebParser ruliwebParser = new RuliwebParser();
            ruliwebParser.parseList(response, mArticleList);
        } else if (mSiteData.getUrl().contains("clien")) {
            ClienParser clienParser = new ClienParser();
            clienParser.parseList(response, mArticleList);
        } else if (mSiteData.getUrl().contains("ppomppu")) {
            PpomppuParser ppomppuParser = new PpomppuParser();
            ppomppuParser.parseList(response, mArticleList);
        }

        renderData();
    }

    private void renderData() {
        //Log.d(mTag, "mMemberList.size(): " + mMemberList.size());

        if (mCurrentPage == 1) {
            //mLoLoading.setVisibility(View.GONE);
            //mPbLoading.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }

        if (mIsDataExists) {
            mAdapter.notifyDataSetChanged();
            mCurrentPage++;
        }

        if (mIsReloadMode) {
            goTop();
            mIsReloadMode = false;
        }

        mIsLoading = false;
        mListener.onArticleListFragmentEvent("onLoadDataFinished");

        mLoLoadMore.setVisibility(View.GONE);
    }

    public boolean goBack() {
        return false;
    }

    public void goTop() {
        //mListView.smoothScrollToPosition(0);
        //mListView.setSelection(0);
        mListView.setSelectionAfterHeaderView();
    }

    public void refresh() {
        //Log.e(mTag, "refresh()......");

        mArticleList.clear();
        mAdapter.notifyDataSetChanged();

        mCurrentPage = 1;

        mEndlessScrollListener.reset();
        mIsReloadMode = true;

        loadData();
    }

    public void openInNew() {
        //String url = mWebView.getUrl();
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        //startActivity(intent);
    }

    public void share() {

    }

    @Override
    public void onPictureClick(int position, String imageUrl) {
        //Log.d(mTag, imageUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
        startActivity(intent);
    }

    @Override
    public void onTitleClick(int position) {

    }

    @Override
    public void onCloseClick(int position) {
        mArticleList.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        //BaseApplication.getInstance().cancelPendingRequests(mVolleyTag);
    }
}
