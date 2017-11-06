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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodayhumorParser extends BaseParser {

    public void parseList(String response, ArrayList<ArticleListData> dataList) {
        /*
        <a href="view.php?table=bestofbest&no=363965&page=1">
            <div class="listLineBox list_tr_sisa" mn='754830'>
                <div class="list_iconBox">
                        <div class='board_icon_mini sisa' style='align-self:center'></div>
                </div>
                <div>
                    <span class="list_no">363965</span>
                    <span class="listDate">2017/09/22 11:45</span>
                    <span class="list_writer" is_member="yes">carryon</span>
                </div>
                <div>
                    <h2 class="listSubject" >네이버를 조져야됨..<span class="list_comment_count"> <span class="memo_count">[3]</span></span></h2>
                </div>
                <div>
                    <span class="list_viewTitle">조회:</span><span class="list_viewCount">1374</span>	            <span class="list_okNokTitle">추천:</span><span class="list_okNokCount">53</span>
                    <span class="list_iconWrap">
                    </span>
                </div>
            </div>
        </a>
        */

        //Log.d(mTag, response);

        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);
        Element root = doc.select("#remove_favorite_alert_div").first();

        if (root != null) {

            for (Element row : doc.select("a")) {
                String title = "";
                String commentCount = "";
                String recommendCount = "";
                String url = "";

                Element el = row.select(".listSubject").first();
                if (el == null) {
                    continue;
                }
                title = el.text();
                //title = title.replaceAll("[0-9]", "").replace("[]", "");

                //Element a = row; //row.select("a").first();
                url = row.attr("href");
                url = "http://m.todayhumor.co.kr/" + url;

                el = row.select(".memo_count").first();
                if (el != null) {
                    String str = el.text();
                    title = title.replace(str, "");
                    title = title.trim();

                    str = str.replace("[", "").replace("]", "");
                    commentCount = str;
                }

                el = row.select(".list_okNokCount").first();
                if (el != null) {
                    recommendCount = " (+" + el.text() + ")";
                }

                title = title + recommendCount;

                //Log.d(mTag, title + " / " + like);

                ArticleListData data = new ArticleListData();
                data.setTitle(title);
                data.setCommentCount(commentCount);
                data.setRecommendCount(recommendCount);
                data.setUrl(url);
                dataList.add(data);
            }
        }
    }

    public ArticleDetailData parseDetail(String response) {
        ArticleDetailData data = new ArticleDetailData();

        Document doc = Jsoup.parse(response);

        Element root = doc.select(".viewContent").first();

        //-----------------------------------------------------------------------------------------------------
        // https://stackoverflow.com/questions/26346698/parsing-html-into-formatted-plaintext-using-jsoup
        //-----------------------------------------------------------------------------------------------------
        //HtmlToPlainText toPlainText = new HtmlToPlainText();

        String regex = "var parent_table = \"(\\w+)\";";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            //Log.d(mTag, "parentTable: " + matcher.group(1));
            data.setTable(matcher.group(1));
        }

        regex = "var parent_id = \"(\\w+)\";";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(response);
        while (matcher.find()) {
            //Log.d(mTag, "parentId: " + matcher.group(1));
            data.setId(matcher.group(1));
        }

        if (root != null) {
            String content = root.html();
            //Log.e(mTag, "원본\n" + content);

            if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                String search = "";
                String replace = "";

                // 용량이 큰 이미지 처리하기
                //<div class="big_img_replace_div" img_id="" img_src="http://thimg.todayhumor.co.kr/upfile/201711/150950210796101714370b4ebdab22bf5a36fc3ca6__mn171930__w720__h17650__f2493985__Ym201711.jpg" img_filesize="2493985">
                search = "<div\\sclass=\"big_img_replace_div\"\\s.+\\simg_src=\"(.+)\"\\simg_filesize=\".*\">";
                replace = "<a href=\"$1\">이미지 보기</a>";
                content = content.replaceAll(search, replace);

                // 비디오 태그
                /*
                <video class="anigif_html5_video" style="max-width:100%" loop="" muted="" playsinline="" webkit-playsinline="" preload="auto" autoplay=""
                    poster="http://thimg.todayhumor.co.kr/upfile/201710/15094200724cb25252048a4361a87168c0dae375ee__mn637066__w225__h400__f2062405__Ym201710__ANIGIF.jpg"
                    data-setup="{" example_option':true}'="" height="__h400" width="__w225">
                    <source src="http://thimg.todayhumor.co.kr/upfile/201710/15094200724cb25252048a4361a87168c0dae375ee__mn637066__w225__h400__f2062405__Ym201710__ANIGIF.mp4" type="video/mp4">
                </video>
                */
                search = "<video\\s.+\\sposter=\"(.+)\"\\s*data-setup=\".+\">\\s*<source\\ssrc=\"(.+)\"\\s.+>\\s*</video>";
                replace = "<a href=\"$2\"><img src=\"$1\"></a> <small><font color=\"#888888\">mp4</font></small>";
                content = content.replaceAll(search, replace);

                // 유튜브
                parseYoutube(root, content, null);

                // 공백 제거
                //content = content.replaceAll("<span[^>|.]*>(<br>)?</span>", "");
                //content = content.replaceAll("<font[^>|.]?>\\s*</font>", "");
                //content = content.replaceAll("\\s*<div[^>|.]*>\\s*(<br>|&nbsp;)*\\s*</div>\\s*", "");
                //content = content.replaceAll("\\s*</div>\\s*(<br>|&nbsp;)*\\s*<div[^>|.]*>\\s*", "");
                //content = content.replaceAll("\\s*<br>\\s*<br>\\s*", "");
            } else {
                ArrayList<MediaData> mediaDatas = new ArrayList<>();

                //<div class='big_img_replace_div' img_id='' img_src='http://cdn.loonastatic.com//img/user/gif/0/1/5/0/0150779558318979.gif' img_filesize='3443962'>
		        //  <div  style='display:table-cell; vertical-align: middle;'>
                //    <div>대용량 이미지입니다.<br>확인하시려면 클릭하세요.<br>크기 : 3.28 MB</div>
                //  </div>
	            //</div>
                //regex = "<div[^>|.]+img_src='(.*)'>\\s*<div[^>|.]+>\\s*<div>.+</div>\\s*</div>\\s*</div>";
                //pattern = Pattern.compile(regex);
                //matcher = pattern.matcher(content);
                //while (matcher.find()) {
                //    Log.d(mTag, "img_src: " + matcher.group(1));
                //    //data.setId(matcher.group(1));
                //}
                for (Element el : root.select(".big_img_replace_div")) {
                    String src = el.attr("img_src");
                    //Log.e(mTag, "big_img_replace_div: " + src);
                    content = content.replace(el.outerHtml(), ""); // 태그는 내용에서 제거
                    addMediaData(mediaDatas, src, src, null);
                }

                // 이미지 태그 목록
                for (Element el : root.select("img")) {
                    String src = el.attr("src");
                    //Log.e(mTag, "img.src: " + src);
                    //Log.e(mTag, "img.outerHtml(): " + img.outerHtml());
                    content = content.replace(el.outerHtml(), ""); // 태그는 내용에서 제거
                    addMediaData(mediaDatas, src, src, null);
                }

                // 유튜브
                parseYoutube(root, content, mediaDatas);

                // 비디오 태그 목록
                for (Element el : root.select("video")) {
                    String src = el.attr("poster");
                    //Log.e(mTag, "video.src: " + src);

                    content = content.replace(el.outerHtml(), ""); // 태그는 내용에서 제거

                    addMediaData(mediaDatas, src, src, null);
                }

                data.setMediaDatas(mediaDatas);

            }

            content = content.trim();

            // 맨 앞, 맨 끝 <br> 잘라내기
            content = content.replaceAll("^(<br>)", "");
            content = content.replaceAll("(<br>|<br />)$", "");

            if (!BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                content = Html.fromHtml(content).toString();
                //content = content.replaceAll("\\s\\s", " ");
            }

            //Log.e(mTag, "결과...\n" + content);

            data.setContent(content);
        }

        return data;
    }

    public ArrayList<CommentData> parseComment(String response) {
        /*
        <a href="view.php?table=bestofbest&no=363965&page=1">
            <div class="listLineBox list_tr_sisa" mn='754830'>
                <div class="list_iconBox">
                        <div class='board_icon_mini sisa' style='align-self:center'></div>
                </div>
                <div>
                    <span class="list_no">363965</span>
                    <span class="listDate">2017/09/22 11:45</span>
                    <span class="list_writer" is_member="yes">carryon</span>
                </div>
                <div>
                    <h2 class="listSubject" >네이버를 조져야됨..<span class="list_comment_count"> <span class="memo_count">[3]</span></span></h2>
                </div>
                <div>
                    <span class="list_viewTitle">조회:</span><span class="list_viewCount">1374</span>
                    <span class="list_okNokTitle">추천:</span><span class="list_okNokCount">53</span>
                    <span class="list_iconWrap">
                    </span>
                </div>
            </div>
        </a>
        */

        //Log.d(mTag, response);

        ArrayList<CommentData> dataList = new ArrayList<>();

        if (response == null || response.isEmpty()) {
            return dataList;
        }

        try {
            JSONObject jsonObject = new JSONObject(response);

            // Getting JSON Array node
            JSONArray jsonArray = jsonObject.getJSONArray("memos");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String thumbnail = "";
                String search = "";
                String replace = "";

                String recommend = obj.getString("ok");
                int recommendCount = 0;
                if (recommend != null && !recommend.isEmpty()) {
                    recommendCount = Integer.parseInt(recommend.replaceAll(",", ""));
                }
                if (recommendCount < 10) {
                    continue;
                }

                String content = obj.getString("memo").trim();
                //Log.e(mTag, "원본\n" + content);

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

                /*
                // 내용을 일반 텍스트로 변환
                content = content.replaceAll("\\s*<img.+?>\\s*(<br>)*\\s*", "");
                content = content.replaceAll("\\s*(&nbsp;)+\\s*", " ");
                content = content.replaceAll("\\s*(&gt;)+\\s*", ">");
                content = content.replaceAll("\\s*(&lt;)+\\s*", "<");
                content = content.replaceAll("<br />", "\n");
                content = content.replaceAll("<br>", "");
                */

                content = content.trim();

                if ("<br>".equals(content) || "<br><br />".equals(content)) {
                    content = "";
                } else {
                    // 맨 앞, 맨 끝 <br> 잘라내기
                    content = content.replaceAll("^(<br>)", "");
                    content = content.replaceAll("(<br>|<br />)$", "");
                }

                if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                    content = content + " <font color=\"#888888\">(+" + recommend + ")</font>";
                } else {
                    content = Html.fromHtml(content).toString();
                    content = content + " (+" + recommend + ")";
                }

                //Log.e(mTag, "결과\n" + content);

                CommentData data = new CommentData();
                data.setThumbnail(thumbnail);
                data.setUrl(thumbnail);
                data.setContent(content);
                dataList.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}