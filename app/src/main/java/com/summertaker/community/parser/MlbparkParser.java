package com.summertaker.community.parser;

import android.text.Html;
import android.util.Log;

import com.summertaker.community.common.BaseApplication;
import com.summertaker.community.common.BaseParser;
import com.summertaker.community.data.ArticleDetailData;
import com.summertaker.community.data.ArticleListData;
import com.summertaker.community.data.CommentData;
import com.summertaker.community.data.MediaData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class MlbparkParser extends BaseParser {

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

        Document doc = Jsoup.parse(response);
        Element root = doc.select(".lists").first();

        if (root != null) {
            //Log.e(mTag, root.html());

            for (Element row : root.select("li")) {
                String title = "";
                String url = "";

                Element a = row.select("a").first();
                if (a == null) {
                    continue;
                }
                url = a.attr("href");

                title = a.text();

                //Log.e(mTag, title);

                ArticleListData data = new ArticleListData();
                data.setTitle(title);
                data.setUrl(url);
                dataList.add(data);
            }
        }
    }

    public void parseDetail(String response, ArticleDetailData detailData) {

        Document doc = Jsoup.parse(response);

        Element root = doc.select("#contentDetail").first();

        parseDetail(root, detailData);

        //Log.e(mTag, "결과...\n" + detailData.getContent());
    }

    public ArrayList<CommentData> parseComment(String response) {
        /*
        [{
            "boardCd":"park",
            "boardSn":11502341,
            "comment":"<p>공감합니다.</p>",
            "oriComment":"<p>공감합니다.</p>",
            "commentCount":21,
            "commentSn":84724199,
            "imageLocation":null,
            "images":[],
            "insertDate":"2017-12-04 08:29:59",
            "ip":"175.♡.18.248",
            "likeCount":4,
            "member":{"nick":"안드레이","nickImageUrl":"","userId":"andy4lee"},
            "bizInfo":null,
            "status":"S61",
            "updateDate":"2017-12-04T08:29:59.000+0900",
            "reCommentSn":84724199,
            "reCommentCount":null,
            "todayYn":true
        },
        */

        //Log.e(mTag, "response: " + response);

        ArrayList<CommentData> dataList = new ArrayList<>();
        if (response == null || response.isEmpty()) {
            return dataList;
        }

        try {
            JSONArray jsonArray = new JSONArray(response);
            //Log.e(mTag, "jsonArray.length(): " + jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String thumbnail = "";
                String search = "";
                String replace = "";

                //String recommend = obj.getString("ok");
                //int recommendCount = 0;
                //if (recommend != null && !recommend.isEmpty()) {
                //    recommendCount = Integer.parseInt(recommend.replaceAll(",", ""));
                //}
                //if (recommendCount < 10) {
                //    continue;
                //}

                String content = obj.getString("comment").trim();
                //Log.e(mTag, "원본\n" + content);

                String likeCountString = obj.getString("likeCount").trim();
                int likeCount = Integer.parseInt(likeCountString);
                if (likeCount < 10) {
                    continue;
                }

                if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                    // 이미지 태그
                    //<img src='data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsQAAA7EAZUrDhsAAAANSURBVBhXYzh8+PB/AAffA0nNPuCLAAAAAElFTkSuQmCC' width='640' height='256' filesize='28423' data-original=\"http://thimg.todayhumor.co.kr/upfile/201710/1509432182851bf9d09dba400aba01aa71aad35115__mn740020__w640__h256__f28423__Ym201710.jpg\" class=\" lazy\">
                    search = "<img\\s.+\\sdata-original=\"(.+)\"\\sclass=\"\\slazy\">";
                    replace = "<img src=\"$1\"><br>";
                    content = content.replaceAll(search, replace);

                    // 비디오 태그
                    /*
                    <video class="anigif_html5_video" style="max-width:100%" loop="" muted="" playsinline="" webkit-playsinline="" preload="auto" autoplay=""
                        poster="http://thimg.todayhumor.co.kr/upfile/201710/15094200724cb25252048a4361a87168c0dae375ee__mn637066__w225__h400__f2062405__Ym201710__ANIGIF.jpg"
                        data-setup="{" example_option':true}'="" height="__h400" width="__w225">
                        <source src="http://thimg.todayhumor.co.kr/upfile/201710/15094200724cb25252048a4361a87168c0dae375ee__mn637066__w225__h400__f2062405__Ym201710__ANIGIF.mp4" type="video/mp4">
                    </video>
                    */
                    search = "<video\\s.+\\sposter='(.+)'\\s*data-setup='.+'>\\s*<source\\ssrc='(.+)'\\s.+>\\s*</video>";
                    replace = "<a href=\"$2\"><img src=\"$1\"></a> <small><font color=\"#888888\">mp4</font></small><br>";
                    content = content.replaceAll(search, replace);

                    // GIF
                    // <img src="http://thimg.todayhumor.co.kr/upfile/201711/15095095624a1f269cd1954d83b2235e1eeab98b34__mn741650__w320__h300__f84232__Ym201711__ANIGIF.gif" filesize="84232"
                    // data-original="http://thimg.todayhumor.co.kr/upfile/201711/15095095624a1f269cd1954d83b2235e1eeab98b34__mn741650__w320__h300__f84232__Ym201711__ANIGIF.gif" class=" lazy" style="display: inline;" height="300" width="320">
                    search = "<img\\ssrc=\"(.+\\.gif)\"\\s*.+>";
                    replace = "<a href=\"$1\"><font color=\"#006600\">[GIF 보기]</font></a>";
                    content = content.replaceAll(search, replace);

                } else {
                    // IMG 태그 찾기
                    Document html = Jsoup.parse(content);
                    for (Element img : html.select("img")) {
                        thumbnail = img.attr("data-original");
                        //Log.e(mTag, "thumbnail: " + thumbnail);

                        // 따옴표 처리하기 (겹따옴표와 홑따옴표가 혼재되어 있음 ㅡㅡ+)
                        search = img.outerHtml().replaceAll("\"", "'");
                        search = search.replace("data-original='", "data-original=\"");
                        search = search.replace("' class=' lazy'", "\" class=\" lazy\"");
                        //Log.e(mTag, "img.outerHtml(): " + search);

                        content = content.replace(search, "");
                    }
                }

                content = content.replaceAll("<p><br>\n</p>", "");

                content = content.replaceAll("<p[^>]*>", ""); // <p> to <br>
                content = content.replaceAll("</p>", "<br>");

/*
https://stackoverflow.com/questions/3075130/what-is-the-difference-between-and-regular-expressions

Ex) eeeAiiZuuuuAoooZeeee

A.*Z yields 1 match: AiiZuuuuAoooZ

A.*?Z yields 2 matches: AiiZ and AoooZ
*/

                content = content.replaceAll("<br>\\s*?<br>", "");
                content = content.replaceAll("(<br>)$", ""); // 맨 끝 <br> 제거

                content = content + " (+" + likeCountString + ")";

                //Log.e(mTag, "결과\n" + content);

                CommentData data = new CommentData();
                data.setThumbnail(thumbnail);
                data.setUrl(thumbnail);
                data.setContent(content);
                dataList.add(data);
            }
        } catch (JSONException e) {
            Log.e(mTag, e.toString());
            //e.printStackTrace();
        }

        return dataList;
    }
}
