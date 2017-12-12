package com.summertaker.community.common;

import android.text.Html;
import android.util.Log;

import com.summertaker.community.data.ArticleDetailData;
import com.summertaker.community.data.CommentData;
import com.summertaker.community.data.MediaData;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseParser {

    protected String mTag;

    public BaseParser() {
        mTag = "=== " + this.getClass().getSimpleName();
    }

    public MediaData getMediaData(String response) {
        return new MediaData();
    }

    protected void parseDetail(Element root, ArticleDetailData data) {
        if (root == null) {
            return;
        }

        String content = root.html();
        //Log.e(mTag, "원본\n" + content);

        ArrayList<MediaData> mediaDatas = new ArrayList<>();

        if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
            Elements imgs = root.select("img");
            if (imgs.size() == 1) {
                // 이미지가 한 개 있는 경우
                Element img = root.select("img").first();
                String src = img.attr("src");
                addMediaData(mediaDatas, src, src, null);
                content = content.replace(img.outerHtml(), ""); // 태그 제거
            } else {
                for (Element img : imgs) {
                    String height = img.attr("height").trim();
                    if (!height.isEmpty()) {
                        height = img.attr("data-img-height").trim();
                    }
                    if (!height.isEmpty()) {
                        int h = Integer.parseInt(height);
                        if (h > 1000) {
                            String src = img.attr("src");
                            //Log.e(mTag, "src: " + src);
                            addMediaData(mediaDatas, src, src, null);
                            content = content.replace(img.outerHtml(), ""); // 태그 제거
                        }
                    }
                }
            }
        } else {
            // 이미지 태그 목록
            for (Element el : root.select("img")) {
                String src = el.attr("src");
                content = content.replace(el.outerHtml(), ""); // 태그 제거
                addMediaData(mediaDatas, src, src, null);
            }
        }

        String search = "";
        String replace = "";

        // 비디오 태그
        search = "<video\\s.+\\sposter=\"(.+)\"\\s*data-setup=\".+\">\\s*<source\\ssrc=\"(.+)\"\\s.+>\\s*</video>";
        replace = "<a href=\"$2\"><img src=\"$1\"></a> <small><font color=\"#888888\">mp4</font></small>";
        content = content.replaceAll(search, replace);

        // 비디오 태그 목록
        for (Element el : root.select("video")) {
            String src = el.attr("poster");
            content = content.replace(el.outerHtml(), ""); // 태그는 내용에서 제거
            addMediaData(mediaDatas, src, src, null);
        }

        // 유튜브
        content = parseYoutube(root, content, mediaDatas);
        content = cleanHtml(content);

        if (!BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
            content = Html.fromHtml(content).toString();
        }

        Log.e(mTag, "결과\n" + content);

        data.setContent(content);
        data.setMediaDatas(mediaDatas);
    }

    private String cleanHtml(String html) {
        // 태그 제거
        html = html.replaceAll("<style>[^<|.]*?</style>", "");

        // 공백 제거
        html = html.replaceAll("\\s{2,}", " ");
        //content = content.replaceAll("(&nbsp;){2,}", "");
        html = html.replaceAll("&nbsp;", "");

        // 속성 제거
        html = html.replaceAll("<h6[^>]*?>", "<h6>");
        html = html.replaceAll("<div[^>]*?>", "<div>");
        html = html.replaceAll("<p[^>]*?>", "<p>");
        html = html.replaceAll("<span[^>]*?>", "<span>");

        // 빈 줄 없애기
        html = html.replaceAll("<p>[\\s|<br>]*</p>", "");

        html = html.replaceAll("<span>(<br>)*?</span>", "");
        html = html.replaceAll("<div>(<br>)*?</div>", "");
        html = html.replaceAll("<div>\\s*?</div>", "");
        html = html.replaceAll("<div>\\s*?</div>", "");
        html = html.replaceAll("<div>\\s*<br>*\\s*</div>", "");

        html = html.trim();

        return html;
    }

    protected String parseYoutube(Element root, String content, ArrayList<MediaData> mediaDatas) {
        String result = content;

        //------------------------------------------------
        // 유튜브 URL 목록 (1)
        //------------------------------------------------
        String regex = "https\\://www\\.youtube\\.com/watch\\?v=([_|\\-|\\w]+)"; // \\w : 알파벳이나 숫자
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(result);   // get a matcher object
        while (matcher.find()) {
            String url = matcher.group();
            //Log.e(mTag, "url: " + url);

            String id = matcher.group(1);
            String src = "https://img.youtube.com/vi/" + id + "/0.jpg";
            //Log.e(mTag, "thumbnail: " + src);

            //if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
            //    String search = "<iframe[^>|.]*src=\"" + regex + "\"[^>|.]*></iframe>";
            //    String replace = "<a href=\"https://m.youtube.com/watch?v=" + id + "\"><img src=\"" + src + "\"></a> <small><font color=\"#888888\">Youtube</font></small>";
            //    content = content.replaceAll(search, replace);
            //} else {
            addMediaData(mediaDatas, src, null, url);
            result = result.replace(url, "");
            //}
        }

        for (Element iframe : root.select("iframe")) {
            String url = iframe.attr("src");
            //Log.e(mTag, "url: " + url);

            regex = "https\\://www\\.youtube\\.com/embed/([_|\\-|\\w]+)"; // \\w : 알파벳이나 숫자
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(url);   // get a matcher object

            String src = null;
            while (matcher.find()) {
                String id = matcher.group(1);
                src = "https://img.youtube.com/vi/" + id + "/0.jpg";
                //Log.e(mTag, "src: " + src);

                //if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
                //    String search = "<iframe[^>|.]*src=\"" + regex + "\"[^>|.]*></iframe>";
                //    String replace = "<a href=\"https://m.youtube.com/watch?v=" + id + "\"><img src=\"" + src + "\"></a> <small><font color=\"#888888\">Youtube</font></small>";
                //    content = content.replaceAll(search, replace);
                //} else {
                //}
            }
            if (src != null) {
                addMediaData(mediaDatas, src, null, url);
            }
            //Log.e(mTag, "iframe.outerHtml(): " + iframe.outerHtml());
            result = result.replace(iframe.outerHtml(), "");
        }

        return result;
    }

    protected String getYoutubeHtml(String content) {
        String result = content;

        // 유튜브 IFRAME
        // <iframe width="700" height="450" src="https://www.youtube.com/embed/rrRDXIenadI?list=WL" frameborder="0"></iframe>
        String regex = "https\\://www\\.youtube\\.com/embed/([_|\\-|\\w]+)"; // \\w : 알파벳이나 숫자
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(result);   // get a matcher object
        while (matcher.find()) {
            String id = matcher.group(1);
            String src = "https://img.youtube.com/vi/" + id + "/0.jpg";
            //Log.e(mTag, "src: " + src);

            String search = "<iframe[^>|.]*src=\"" + regex + "\"[^>|.]*></iframe>";
            String replace = "<a href=\"https://m.youtube.com/watch?v=" + id + "\"><img src=\"" + src + "\"></a> <small><font color=\"#888888\">Youtube</font></small>";
            result = result.replaceAll(search, replace);
        }
        return result;
    }

    protected void addMediaData(ArrayList<MediaData> mediaDatas, String thumbnail, String image, String url) {
        if (mediaDatas != null) {

            boolean isExist = false;
            if (url != null) {
                for (MediaData md : mediaDatas) {
                    if (md.getUrl() != null && url.equals(md.getUrl())) {
                        isExist = true;
                        break;
                    }
                }
            }

            if (!isExist) {
                MediaData mediaData = new MediaData();
                mediaData.setThumbnail(thumbnail);
                mediaData.setImage(image);
                mediaData.setUrl(url);

                mediaDatas.add(mediaData);
            }
        }
    }
}