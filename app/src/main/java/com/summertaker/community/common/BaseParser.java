package com.summertaker.community.common;

import android.util.Log;

import com.summertaker.community.data.MediaData;

import org.jsoup.nodes.Element;

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