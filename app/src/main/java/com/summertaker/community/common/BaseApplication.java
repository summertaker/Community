package com.summertaker.community.common;

import android.app.Application;
import android.content.SharedPreferences;
import android.text.TextUtils;

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

    public boolean SETTINGS_USE_IMAGE_GETTER = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        loadSettings();

        USER_AGENT_WEB = "Mozilla/5.0 (Macintosh; U; Mac OS X 10_6_1; en-US) ";
        USER_AGENT_WEB += "AppleWebKit/530.5 (KHTML, like Gecko) ";
        USER_AGENT_WEB += "Chrome/ Safari/530.5";

        USER_AGENT_MOBILE = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) ";
        USER_AGENT_MOBILE += "AppleWebKit/528.18 (KHTML, like Gecko) ";
        USER_AGENT_MOBILE += "Version/4.0 Mobile/7A341 Safari/528.16";

        mSiteList = new ArrayList<>();

        mSiteList.add(new SiteData("오유베오베", USER_AGENT_MOBILE, "http://m.todayhumor.co.kr/list.php?table=bestofbest", "&page="));
        mSiteList.add(new SiteData("보배베스트", USER_AGENT_MOBILE, "http://m.bobaedream.co.kr/board/new_writing/best", "/"));
        mSiteList.add(new SiteData("클리앙공감", USER_AGENT_MOBILE, "https://m.clien.net/service/group/board_all?&od=T33", "&po="));
        mSiteList.add(new SiteData("클리앙소식", USER_AGENT_MOBILE, "https://m.clien.net/service/board/news?&od=T31", "&po="));
        mSiteList.add(new SiteData("클리앙팁", USER_AGENT_MOBILE, "https://m.clien.net/service/board/lecture?&od=T31", "&po="));
        mSiteList.add(new SiteData("루리웹PC", USER_AGENT_MOBILE, "http://m.ruliweb.com/news/board/1003/list", "?page="));
        mSiteList.add(new SiteData("루리웹모바일", USER_AGENT_MOBILE, "http://m.ruliweb.com/news/board/1004/list", "?page="));
        mSiteList.add(new SiteData("루리웹콘솔", USER_AGENT_MOBILE, "http://m.ruliweb.com/news/board/1001/list", "?page="));
        mSiteList.add(new SiteData("루리웹사정경", USER_AGENT_MOBILE, "http://m.ruliweb.com/news/board/300018/list", "?page="));
        //mSiteList.add(new SiteData("뽐뿌인기", USER_AGENT_MOBILE, "http://m.ppomppu.co.kr/new/pop_bbs.php", "?page="));

        // 사진 외부 링크 제한 걸려있음
        //mSiteList.add(new SiteData("엠팍추천", USER_AGENT_MOBILE, "http://mlbpark.donga.com/mp/best.php?b=bullpen&m=like", null));

        //mSiteList.add(new SiteData("루리웹베스트", USER_AGENT_MOBILE, "http://m.ruliweb.com/best", "?page="));
        //mSiteList.add(new SiteData("루리웹힛갤", USER_AGENT_MOBILE, "http://m.ruliweb.com/best/selection", "?page="));
        //mSiteList.add(new SiteData("루리웹스샷", USER_AGENT_MOBILE, "http://m.ruliweb.com/news/board/1008", "?page="));

        //mSiteList.add(new SiteData("웃대오늘베", "http://m.humoruniv.com/board/list.html?table=pds&st=day&pg=0"));
        //mSiteList.add(new SiteData("SLR클럽추천", USER_AGENT_MOBILE, "http://www.slrclub.com/bbs/zboard.php?id=best_article&category=1&setsearch=category", ""));
        //mSiteList.add(new SiteData("SLR클럽인기", "http://www.slrclub.com/bbs/zboard.php?id=hot_article&category=1&setsearch=category"));

        //mSiteList.add(new SiteData("재팬스퀘어", USER_AGENT_MOBILE, "http://theqoo.net/index.php?mid=japan&filter_mode=normal&category=26063", "&page="));
        //mSiteList.add(new SiteData("사카미치", USER_AGENT_MOBILE, "http://theqoo.net/index.php?mid=jdol&filter_mode=normal&category=29770", "&page="));
        //mSiteList.add(new SiteData("48스퀘어", USER_AGENT_MOBILE, "http://theqoo.net/index.php?mid=talk48&filter_mode=normal&category=161632742", "&page="));
        //mSiteList.add(new SiteData("48돌", USER_AGENT_MOBILE, "http://theqoo.net/dol48?filter_mode=normal", "&page="));
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

    public void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(getApplicationContext().getPackageName(), MODE_PRIVATE);
        SETTINGS_USE_IMAGE_GETTER = prefs.getBoolean("USE_IMAGE_GETTER", false); //키값, 디폴트값
    }

    public void saveSettings() {
        SharedPreferences pref = getSharedPreferences(getApplicationContext().getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("USE_IMAGE_GETTER", BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER);
        editor.apply();
    }
}
