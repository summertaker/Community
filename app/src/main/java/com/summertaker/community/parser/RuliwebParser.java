package com.summertaker.community.parser;

import android.text.Html;
import android.util.Log;

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

public class RuliwebParser extends BaseParser {

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
        Element root = doc.select(".board_list_table").first();

        if (root != null) {
            for (Element row : root.select(".row")) {
                String title = "";
                String commentCount = "";
                String recommendCount = "";
                String url = "";

                Element el;

                el = row.select("a.cate_label").first();
                if (el == null) {
                    continue; // 공지사항은 출력하지 않는다.
                }

                el = row.select("a.subject_link").first();
                if (el == null) {
                    continue;
                }
                title = el.text();
                //title = title.replaceAll("[0-9]", "").replace("[]", "");

                //Element a = row; //row.select("a").first();
                url = el.attr("href");

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

    public void parseDetail(String response, ArticleDetailData articleDetailData, ArrayList<CommentData> commentList) {

        Document doc = Jsoup.parse(response);
        Element root = doc.select(".board_main_view").first();

        //-----------------------------------------------------------------------------------------------------
        // https://stackoverflow.com/questions/26346698/parsing-html-into-formatted-plaintext-using-jsoup
        //-----------------------------------------------------------------------------------------------------
        //HtmlToPlainText toPlainText = new HtmlToPlainText();

        //ArticleDetailData articleDetailData = new ArticleDetailData();

        if (root != null) {
            Element el;

            // 원본 출처
            String sourceHtml = "";
            el = root.select(".source_url").first();
            if (el != null) {
                Element a = el.select("a").first();
                String url = a.attr("href");
                String urlString = (url.length() > 25) ? url.substring(0, 25) + "..." : url;
                sourceHtml = "<p>출처: <a href=\"" + url + "\">" + urlString + "</a></p>";
            }

            // 본문 내용
            el = root.select(".view_content").first();

            String content = el.html();
            Log.e(mTag, "원본\n" + content);

            content = content.replaceAll("src=\"//", "src=\"http://");

            content = sourceHtml + content;

            ArrayList<MediaData> mediaDatas = new ArrayList<>();

            /*
            //---------------------
            // 이미지 태그 목록
            //---------------------
            //int imgCount = 0;
            for (Element el : root.select("img")) {
                String src = el.attr("src");
                //Log.e(mTag, src);

                src = "http:" + src;

                addMediaData(mediaDatas, src, src, null);
            }
            */

            //--------------------------------------
            // 유튜브 썸네일 사진과 링크 파싱하기
            //--------------------------------------
            parseYoutube(root, content, mediaDatas);

            articleDetailData.setMediaDatas(mediaDatas);

            //content = content.replaceAll("<img.+?>", "");
            //content = content.replaceAll("\\s*<p\\s*(.|\")*>\\s*</p>\\s*", "<br>");
            //content = content.replaceAll("<(p|span|div)[^>]*>\\s*(<br>|&nbsp;)*\\s*</(p|span|div)>", "<br>");

            //content = Html.fromHtml(content).toString();

            //Log.e(mTag, "결과\n" + content);
            articleDetailData.setContent(content);

            //data.setThumbnails(thumbnails);
            //data.setTargets(images);
        }

        //return articleDetailData;

        parseComment(doc, commentList);
    }

    /**
     * 댓글 파싱하기
     */
    private void parseComment(Document doc, ArrayList<CommentData> commentList) {
        Element root = doc.select(".comment_view_wrapper").first();

        for (Element div : root.select(".comment_view")) {
            for (Element table : div.select(".comment_table")) {
                for (Element tr : table.select(".comment_element")) {
                    String content = "";
                    boolean isBest = false;
                    boolean isReply = false;

                    Element el;

                    el = tr.select("span.text").first();
                    content = el.text();

                    el = tr.select(".icon_best").first(); // 베스트 댓글인 경우
                    //String bestString = "";
                    if (el != null) {
                        //bestString = "[베스트] ";
                        isBest = true;
                    }
                    //content = bestString + content;

                    el = tr.select(".is_child").first(); // 댓글의 댓글인 경우
                    String replyString = "";
                    if (el != null) {
                        replyString = "Re. ";
                        isReply = true;
                    }
                    content = replyString + content;

                    CommentData commentData = new CommentData();
                    commentData.setContent(content);
                    commentData.setBest(isBest);
                    commentData.setReply(isReply);
                    commentList.add(commentData);
                }
            }
        }
    }
}
