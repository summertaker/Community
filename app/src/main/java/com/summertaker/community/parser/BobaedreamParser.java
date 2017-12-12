package com.summertaker.community.parser;

import android.text.Html;
import android.util.Log;

import com.summertaker.community.common.BaseApplication;
import com.summertaker.community.common.BaseParser;
import com.summertaker.community.data.ArticleDetailData;
import com.summertaker.community.data.ArticleListData;
import com.summertaker.community.data.CommentData;
import com.summertaker.community.data.MediaData;
import com.summertaker.community.util.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class BobaedreamParser extends BaseParser {

    public void parseList(String response, ArrayList<ArticleListData> dataList) {
        /*
        <li class='items'>
            <div class='photo' >
                <span class='icon_rank'>1</span>
            </div>
            <div class='text'>
                <div class='title'>
                    <a href='http://mlbpark.donga.com/mp/b.php?b=bullpen&id=201712030011630101&m=view' alt=''>유아인 드디어 전면전을 선포하나 보군요</a>
                </div>
                <div class='info'>
                    <span class='nick'>wooks90</span>
                    <span class='date'>2017-12-03</span>
                </div>
            </div>
        </li>
        */
        //Log.d(mTag, response);

        if (response == null || response.isEmpty()) {
            return;
        }

        response = Util.convertedString(response, null);

        Document doc = Jsoup.parse(response);
        Element root = doc.select(".rank").first();

        if (root != null) {
            //Log.e(mTag, root.html());

            for (Element row : root.select("li")) {
                String title = "";
                String url = "";
                String recommendCount = "";

                Element a = row.select("a").first();
                if (a == null) {
                    continue;
                }
                url = "http://m.bobaedream.co.kr" + a.attr("href");

                Element el = a.select(".cont").first();
                title = el.text();

                el = a.select(".txt2").first();
                recommendCount = el.select("span").get(3).text();
                recommendCount = recommendCount.replace("추천", "").trim();

                //Log.e(mTag, title + " / " + recommendCount);

                title = title + " (+" + recommendCount + ")";

                ArticleListData data = new ArticleListData();
                data.setTitle(title);
                data.setUrl(url);
                data.setRecommendCount(recommendCount);
                dataList.add(data);
            }
        }
    }

    public void parseDetail(String response, ArticleDetailData detailData, ArrayList<CommentData> commentList) {

        response = Util.convertedString(response, null);

        Document doc = Jsoup.parse(response);
        Element root = doc.select(".article-body").first();

        parseDetail(root, detailData);

        parseComment(doc, commentList);
    }

    /**
     * 댓글 파싱하기
     */
    private void parseComment(Document doc, ArrayList<CommentData> commentList) {
        Element root = doc.select(".reple_body").first();
        //Log.e(mTag, "root.html(): " + root.html());

        if (root != null) {
            root = root.select(".list").first();

            for (Element row : root.select(".best")) {
                String content = "";
                boolean isBest = false;
                boolean isReply = false;
                String recommend = "";

                //Log.e(mTag, "원본\n" + tr.html());

                Element el;

                el = row.select(".reply").first();
                content = el.html();

                Element ic = el.select(".ico3").first();
                if (ic != null) {
                    content = content.replaceAll(ic.outerHtml(), "");
                }

                el = row.select(".good").first();
                if (el != null) {
                    recommend = el.text();
                    if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                        content = content + " <font color=\"#888888\">(+" + recommend + ")</font>";
                    }
                }

                //el = Jsoup.parse(content);
                //content = el.text();

                if (!BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                    content = Html.fromHtml(content).toString();
                }

                CommentData data = new CommentData();
                data.setContent(content);
                commentList.add(data);
            }
        }
    }
}
