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
                boolean isNotice = false;

                Element el;

                el = row.select("strong").first(); // 공지사항
                if (el != null) {
                    isNotice = true;
                }

                el = row.select("a.subject_link").first();
                if (el == null) {
                    continue;
                }
                title = el.text();
                url = el.attr("href");

                //Log.d(mTag, title + " / " + like);

                if (!isNotice) {
                    ArticleListData data = new ArticleListData();
                    data.setTitle(title);
                    data.setCommentCount(commentCount);
                    data.setRecommendCount(recommendCount);
                    data.setUrl(url);
                    dataList.add(data);
                }
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

            // 원본 출처
            String sourceUrl = "";
            Element source = root.select(".source_url").first();
            if (source != null) {
                Element a = source.select("a").first();
                sourceUrl = a.attr("href");
            }
            //Log.e(mTag, "source:" + sourceUrl);
            articleDetailData.setSource(sourceUrl);

            // 본문 내용
            root = root.select(".view_content").first();

            String content = root.html();
            Log.e(mTag, "원본\n" + content);

            content = content.replaceAll("src=\"//", "src=\"http://"); // 이미지 URL 처리

            if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                //
            } else {
                ArrayList<MediaData> mediaDatas = new ArrayList<>();

                // 이미지 태그 목록
                for (Element el : root.select("img")) {
                    String src = el.attr("src");
                    //Log.e(mTag, "img.src: " + src);

                    content = content.replace(el.outerHtml(), "");

                    addMediaData(mediaDatas, src, src, null);
                }

                parseYoutube(root, content, mediaDatas);

                articleDetailData.setMediaDatas(mediaDatas);
            }

            // H1, H2, H3 ... 글자 크기 리셋
            content = content.replaceAll("<h\\d[^>|.]*>", "<p>");
            content = content.replaceAll("</h\\d>", "</p>");

            // 공백 제거하기
            content = content.replaceAll("\\s*&nbsp;\\s*", "");
            content = content.replaceAll("<p[^>|.]*>\\s*(<br>)*\\s*</p>", "");
            content = content.replaceAll("</p>\\s*<br>", "</p>");

            //content = content.replaceAll("<p[^>]*>", ""); // <p> to <br>
            //content = content.replaceAll("</p>", "<br>");

            //if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
            //    content = Html.fromHtml(content).toString().trim();
            //}

            Log.e(mTag, "결과\n" + content);

            articleDetailData.setContent(content);
        }

        parseComment(doc, commentList);
    }

    /**
     * 댓글 파싱하기
     */
    private void parseComment(Document doc, ArrayList<CommentData> commentList) {
        Element root = doc.select(".comment_table").first();
        //Log.e(mTag, "root.html(): " + root.html());

        if (root != null) {
            //for (Element div : root.select(".comment_view")) {
            //    for (Element table : div.select(".comment_table")) {
            for (Element row : root.select(".comment_element")) {
                String content = "";
                boolean isBest = false;
                boolean isReply = false;
                String recommend = "";

                //Log.e(mTag, "원본\n" + tr.html());

                Element el;

                el = row.select("span.text").first();
                content = el.text();
                //Log.e(mTag, "content: " + content);

                el = row.select(".icon_best").first(); // 베스트 댓글인 경우
                //String bestString = "";
                if (el != null) {
                    //bestString = "[베스트] ";
                    isBest = true;
                }
                //content = bestString + content;

                el = row.select(".btn_like").first();
                el = el.select(".num").first();
                if (el != null) {
                    if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                        recommend = " <font color=\"#888888\">(+" + el.text() + ")</font>";
                    } else {
                        recommend = " (+" + el.text() + ")";
                    }
                }

                el = row.select(".comment_img").first();
                if (el != null) {
                    Log.e(mTag, "img.src: " + el.attr("src"));
                }

                el = row.select(".is_child").first(); // 댓글의 댓글인 경우
                String replyString = "";
                if (el != null) {
                    replyString = "Re. ";
                    isReply = true;
                }
                content = replyString + content + recommend;

                CommentData data = new CommentData();
                data.setContent(content);
                data.setBest(isBest);
                data.setReply(isReply);
                data.setUrl("");
                commentList.add(data);
            }
            //    }
            //}
        }
    }
}
