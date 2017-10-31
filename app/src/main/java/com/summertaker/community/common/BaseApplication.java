package com.summertaker.community.common;

import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.summertaker.community.data.SiteData;

import java.util.ArrayList;
import java.util.List;

public class BaseApplication extends Application {

    private static BaseApplication mInstance;

    public static final String TAG = BaseApplication.class.getSimpleName();

    public String USER_AGENT_WEB;
    public String USER_AGENT_MOBILE;

    private RequestQueue mRequestQueue;

    private List<SiteData> mSiteList;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        USER_AGENT_WEB = "Mozilla/5.0 (Macintosh; U; Mac OS X 10_6_1; en-US) ";
        USER_AGENT_WEB += "AppleWebKit/530.5 (KHTML, like Gecko) ";
        USER_AGENT_WEB += "Chrome/ Safari/530.5";

        USER_AGENT_MOBILE = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) ";
        USER_AGENT_MOBILE += "AppleWebKit/528.18 (KHTML, like Gecko) ";
        USER_AGENT_MOBILE += "Version/4.0 Mobile/7A341 Safari/528.16";

        mSiteList = new ArrayList<>();

        mSiteList.add(new SiteData("베오베", USER_AGENT_MOBILE, "http://m.todayhumor.co.kr/list.php?table=bestofbest", "&page="));

        //mSiteList.add(new SiteData("루리웹힛갤", USER_AGENT_MOBILE, "http://m.ruliweb.com/best/selection", "?page=", 10));
        mSiteList.add(new SiteData("루리웹PC", USER_AGENT_MOBILE, "http://m.ruliweb.com/news/board/1003/list", "?page="));
        mSiteList.add(new SiteData("루리웹모바일", USER_AGENT_MOBILE, "http://m.ruliweb.com/news/board/1004/list", "?page="));
        mSiteList.add(new SiteData("루리웹콘솔", USER_AGENT_MOBILE, "http://m.ruliweb.com/news/board/1001/list", "?page="));

        mSiteList.add(new SiteData("재팬스퀘어", USER_AGENT_MOBILE, "http://theqoo.net/index.php?mid=japan&filter_mode=normal&category=26063", "&page="));
        mSiteList.add(new SiteData("사카미치", USER_AGENT_MOBILE, "http://theqoo.net/index.php?mid=jdol&filter_mode=normal&category=29770", "&page="));
        mSiteList.add(new SiteData("48스퀘어", USER_AGENT_MOBILE, "http://theqoo.net/index.php?mid=talk48&filter_mode=normal&category=161632742", "&page="));
        mSiteList.add(new SiteData("48돌", USER_AGENT_MOBILE, "http://theqoo.net/dol48?filter_mode=normal", "&page="));

        //mSiteList.add(new SiteData("보배베스트", "http://m.bobaedream.co.kr/board/new_writing/best/1"));
        //mSiteList.add(new SiteData("웃대오늘베", "http://m.humoruniv.com/board/list.html?table=pds&st=day&pg=0"));
        //mSiteList.add(new SiteData("뽐뿌핫", "http://m.ppomppu.co.kr/new/hot_bbs.php?page=1"));
        //mSiteList.add(new SiteData("뽐뿌인기", "http://m.ppomppu.co.kr/new/pop_bbs.php?page=1"));
        //mSiteList.add(new SiteData("엠팍최다추천", "http://mlbpark.donga.com/mp/best.php?b=bullpen&m=like"));
        //mSiteData.add(new SiteData("SLR클럽추천", "http://www.slrclub.com/bbs/zboard.php?id=best_article&category=1&setsearch=category"));
        //mSiteData.add(new SiteData("SLR클럽인기", "http://www.slrclub.com/bbs/zboard.php?id=hot_article&category=1&setsearch=category"));
        //mSiteData.add(new SiteData("클리앙공감", "https://m.clien.net/service/group/board_all?od=T33"));
    }

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }

    public String getMobileUserAgent() {
        return USER_AGENT_MOBILE;
    }

    public void setMobileUserAgent(String mobileUserAgent) {
        USER_AGENT_MOBILE = mobileUserAgent;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public List<SiteData> getSiteList() {
        return mSiteList;
    }

    public SiteData getSiteData(int position) {
        return mSiteList.get(position);
    }
}
