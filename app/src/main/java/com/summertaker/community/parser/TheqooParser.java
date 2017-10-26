package com.summertaker.community.parser;

import android.text.Html;
import android.util.Log;

import com.summertaker.community.common.BaseParser;
import com.summertaker.community.data.ArticleDetailData;
import com.summertaker.community.data.ArticleListData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TheqooParser extends BaseParser {

    public void parseList(String response, ArrayList<ArticleListData> dataList) {
        /*
        <ul class="list">
            <li class="clearfix">
			    <a href="/index.php?mid=japan&amp;filter_mode=normal&amp;category=26063&amp;page=2&amp;document_srl=598213046" class="list-link"></a>
                <ul class="list-element">
				    <li class="title">
                        <span style="">GINGER 2017년 12월호 카와구치 하루나</span>
                        <img src="/modules/document/tpl/icons/image.gif" />
                    </li>
                    <li class="date el">14:31</li>
                    <li class="hit el">조회 수 57</li>
                    <li class="hit el">추천 수 0</li>
                    <li class="el" style="color:#ff0000">재팬스퀘어</li>
                </ul>
            </li>
            ...
        */
        /*
        <ul class="list">
            <li class="clearfix has-comment">
                <a href="/index.php?mid=dol48&amp;filter_mode=normal&amp;document_srl=598278129" class="list-link"></a>
                <ul class="list-element">
                    <li class="title">
                        <a href="/index.php?mid=dol48&amp;group_srl=502330701" class='preface'>앗퀘어)</a>
                        <span style="">돈스타스토리(❁´▽`❁)</span>
                    </li>
                    <li class="date el">16:32</li>
                    <li class="hit el">조회 수 22</li>
                    <li class="hit el">추천 수 0</li>
                    <li class="el" style="color:#81bff9">앗짱</li>
                </ul>
                <a href="http://theqoo.net/598278129#comment" class="reply reply_count m-list-reply">1</a>
            </li>
            ...
        */

        //Log.d(mTag, response);

        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);
        //Element root = doc.select("#remove_favorite_alert_div").first();

        Elements rows = doc.select(".list");
        if (rows != null) {

            for (Element row : rows.select("li.clearfix")) {
                String title = "";
                String commentCount = "";
                String recommendCount = "";
                String url = "";

                Element el;

                Element a = row.select("a").first();
                if (a == null) {
                    continue;
                }
                url = a.attr("href");
                url = "http://theqoo.net" + url;

                el = row.select(".title").first();
                if (el == null) {
                    continue;
                }
                title = el.text();

                //Log.e(mTag, title);

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
        /*
        <article>
	        <div class="title-wrap clearfix" style="padding-left:10px">
		        <h3>
		            <span style="">FLASH 2017/11/07 와다 아키코 특별 인터뷰 (과거 사진 &amp; 공연 리허설 사진)</span>
		        </h3>
		        <div class="under-title">
			        <span class="name el nName">무명의 더쿠</span>
			        <span class="l">|</span>
			        <span class="date el">16:04</span>
			        <span class="l">|</span>
			        <span class="hit el">조회 수 17</span>
			    </div>
            </div>
	        <div class="read-body">
				<!--BeforeDocument(598263158,0)-->
				<div class="document_598263158_0 xe_content">
				    <p><img src="http://img.theqoo.net/img/IKbNt.jpg" alt="IKbNt" width="900" height="1183" /><br />
				    <img src="http://img.theqoo.net/img/vszEv.jpg" alt="vszEv" width="900" height="1172" /><br />
				    <img src="http://img.theqoo.net/img/oNTUy.jpg" alt="oNTUy" width="900" height="1176" /><br />
				    <img src="http://img.theqoo.net/img/OhUiw.jpg" alt="OhUiw" width="900" height="1183" /><br />
				    <img src="http://img.theqoo.net/img/LdkBP.jpg" alt="LdkBP" width="900" height="1188" /><br />
				    </p>
				</div><!--AfterDocument(598263158,0)-->
			    <div class="read-file">
				    <h3>File List</h3>
			        <ul>
			        </ul>
		        </div>
            </div>
        </article>
        */
        ArticleDetailData data = new ArticleDetailData();

        Document doc = Jsoup.parse(response);

        Element root = doc.select(".xe_content").first();

        if (root == null) {
            Log.e(mTag, "root[.xe_content] is null...");
        } else {
            //Log.e(mTag, "root: "+root.text());

            String content = root.html();
            //Log.e(mTag, "원본: " + content);

            content = content.replaceAll("\\s*<object\\s*(.|\")*>.*</object>\\s*", ""); // 어플이 멈추기에 일단 지운다.

            ArrayList<String> thumbnails = new ArrayList<>();
            ArrayList<String> images = new ArrayList<>();

            //---------------------
            // 첨부 이미지 목록
            //---------------------
            //int imgCount = 0;
            for (Element el : root.select("img")) {
                String src = el.attr("src");
                //Log.e(mTag, src);

                if (src.contains("//attach.mail.daum.net/") || src.contains("//mail1.daumcdn.net")) {
                    continue;
                }
                thumbnails.add(src);
                images.add(src);

                //imgCount++;
                //if (imgCount > 10) { // 스마트폰 메모리 부족 에러 발생하니 자른다.
                //    break;
                //}
            }

            //----------------------------------------------------------
            // 더쿠 이미지 단축 URL 링크를 이미지로 출력할 수 있도록 변환
            // http://img.theqoo.net/PZfEq
            // http://img.theqoo.net/img/PZfEq.jpg
            //----------------------------------------------------------
            String regex = "http\\://img\\.theqoo\\.net/[0-9a-zA-Z]+";
            Pattern pattern = Pattern.compile(regex);
            Matcher match = pattern.matcher(content);   // get a matcher object
            while (match.find()) {
                String src = match.group();
                //Log.e(mTag, "src: " + match.group());

                if (src.contains(".jpg") || src.contains(".png")) {
                    continue;
                }

                if (src.substring(src.length() - 4).equals("/img")) {
                    continue;
                }

                //Log.e(mTag, "더쿠 IMG: " + src);

                // URL 텍스트는 내용에서 제거해 준다.
                content = content.replace(src, "");

                src = src.replaceAll("</?.+>", "");
                src = src.replace("http://img.theqoo.net/", "http://img.theqoo.net/img/") + ".jpg";

                thumbnails.add(src);
                images.add(src);
            }

            data.setThumbnails(thumbnails);
            data.setImages(images);

            //----------------------
            // 외부 링크 목록 설정
            //----------------------
            ArrayList<String> outLinks = new ArrayList<>();

            //-------------------------------------------------------------------------
            // 외부 링크 (1) - 인스타그램
            // https://www.instagram.com/p/BasYqhFBIb7/
            //-------------------------------------------------------------------------
            regex = "https\\://www\\.instagram\\.com/p/[0-9a-zA-Z]+/?";
            pattern = Pattern.compile(regex);
            match = pattern.matcher(content);   // get a matcher object
            while (match.find()) {
                String src = match.group();
                //Log.e(mTag, "인스타그램: " + src);

                // URL 텍스트는 내용에서 제거해 준다.
                content = content.replace(src, "");
                outLinks.add(src);
            }

            //---------------------------------------------
            // 외부 링크 (2) - 네이버/유튜브 IFRAME 동영상
            //---------------------------------------------
            for (Element el : root.select("iframe")) {
                String src = el.attr("src");
                //Log.e(mTag, "네이버: " + src);

                content = content.replace(src, "");
                outLinks.add(src);
            }

            data.setOutLinks(outLinks);

            //-------------------------------------------------------------------------
            // 트위터 URL 텍스트 제거 - 사용자들이 더쿠에 트위터 이미지를 같이 올려준다.
            //-------------------------------------------------------------------------
            content = content.replaceAll("https://twitter.com/[_|0-9a-zA-Z]+/status/[0-9]{18}", "");

            //---------------------
            // 내용 HTML 만들기
            //---------------------
            //content = toPlainText.getPlainText(root);
            //Log.e(mTag, content);

            //-------------------------------------------
            // https://regexone.com/lesson/whitespaces
            //-------------------------------------------
            // \\s 공백
            // . Any Character
            // (…) Capture Group
            // (.|\") 아무 문자 또는 " 기호
            //-------------------------------------------
            /*
            1)
            <div> <br> </div>
            replaceAll("\\s*<div>\\s*<br>\\s*</div>\\s*", "");

            2)
            <div> &nbsp; </div>
            replaceAll("\\s*<div>\\s*&nbsp;\\s*</div>\\s*", "");

            3) 1)과 2)를 합해서
            replaceAll("\\s*<div>\\s*(<br>|&nbsp;)\\s*</div>\\s*", "");

            4) <div style=""> <br> &nbsp; </div>
            replaceAll("\\s*<div\\s*(.|\")*>\\s*(<br>)*(&nbsp;)*\\s*</div>\\s*", "");
            */
            content = content.replaceAll("\\s*<img.+?>\\s*", "");
            content = content.replaceAll("<p\\s*(.|\")*>\\s*(<br>|&nbsp;)*\\s*</p>", "");
            //content = content.replaceAll("<p\\s*(.|\")*>\\s*<br>\\s*</p>", "");
            content = content.replaceAll("<div\\s*(.|\")*>\\s*<br>\\s*</div>", "");
            content = content.replaceAll("\\s*<br>\\s*<br>\\s*", "");
            //content = content.replaceAll("\\s*<div\\s*(.|\")*>\\s*(<br>)*(&nbsp;)*\\s*</div>\\s*", "");
            //content = content.replaceAll("</div>\\s*<br>\\s*", "</div>");
            //content = content.replaceAll("<br\\s*.*>\\s*<br\\s*.*>\\s*<br\\s*.*>\\s*", "<br><br>");
            //content = content.replaceAll("<br\\s*.*>\\s*<br\\s*.*>\\s*<br\\s*.*>\\s*", "<br><br>");

            String temp = "\\s*<div class=\"read-file\">\\s*<h3>File List</h3>\\s*<ul>\\s*</ul>\\s*</div>\\s*";
            content = content.replaceAll(temp, "").trim();

            Log.e(mTag, "결과: " + content);

            content = Html.fromHtml(content).toString();

            data.setContent(content);
        }

        return data;
    }
}
