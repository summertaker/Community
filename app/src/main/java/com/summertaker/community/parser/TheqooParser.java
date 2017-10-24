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

        Element root = doc.select(".read-body").first();

        if (root == null) {
            Log.e(mTag, "root is null...");
        } else {
            //Log.e(mTag, "root: "+root.text());

            root = root.select("div").first();
            if (root != null) {
                //---------------------
                // 이미지 목록
                //---------------------
                ArrayList<String> thumbnails = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();
                for (Element el : root.select("img")) {
                    String src = el.attr("src");
                    //Log.e(mTag, src);
                    thumbnails.add(src);
                    images.add(src);
                }
                data.setThumbnails(thumbnails);
                data.setImages(images);

                //-----------------------
                // IFRAME 목록
                //-----------------------
                ArrayList<String> iframes = new ArrayList<>();
                for (Element el : root.select("iframe")) {
                    String src = el.attr("src");
                    Log.e(mTag, "iframe: " + src);
                    iframes.add(src);
                }
                data.setIframes(iframes);

                //---------------------
                // 내용 HTML 만들기
                //---------------------
                String content = root.html();
                //Log.e(mTag, content);

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
                표현 replaceAll("\\s*<div>\\s*<br>\\s*</div>\\s*", "");

                2)
                <div> &nbsp; </div>
                표현 replaceAll("\\s*<div>\\s*&nbsp;\\s*</div>\\s*", "");

                3) 1)과 2)를 합해서
                표현 replaceAll("\\s*<div>\\s*(<br>|&nbsp;)\\s*</div>\\s*", "");

                4) <div style=""> <br> &nbsp; </div>
                표현 replaceAll("\\s*<div\\s*(.|\")*>\\s*(<br>)*(&nbsp;)*\\s*</div>\\s*", "");
                */
                content = content.replaceAll("\\s*<img.+?>\\s*", "");
                //content = content.replaceAll("\\s*<div\\s*(.|\")*>\\s*(<br>)*(&nbsp;)*\\s*</div>\\s*", "");
                //content = content.replaceAll("</div>\\s*<br>\\s*", "</div>");
                //content = content.replaceAll("<br\\s*.*>\\s*<br\\s*.*>\\s*<br\\s*.*>\\s*", "<br><br>");
                //content = content.replaceAll("<br\\s*.*>\\s*<br\\s*.*>\\s*<br\\s*.*>\\s*", "<br><br>");

                String trash = "<div class=\"read-file\">\\s*<h3>File List</h3>\\s*<ul>\\s*</ul>\\s*</div>";
                content = content.replaceAll(trash, "");

                content = Html.fromHtml(content).toString().trim();
                //Log.e(mTag, ">>" + content + "<<");

                data.setContent(content);
            }
        }

        return data;
    }
}
