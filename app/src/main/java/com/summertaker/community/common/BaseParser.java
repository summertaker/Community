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
        mTag = "========== " + this.getClass().getSimpleName();
    }

    public MediaData getMediaData(String response) {
        return new MediaData();
    }

    protected void parseYoutube(Element root, String content, ArrayList<MediaData> mediaDatas) {
        //------------------------------------------------
        // 유튜브 URL 목록 (1)
        //------------------------------------------------
        String regex = "https\\://www\\.youtube\\.com/watch\\?v=([_|\\w]+)"; // \\w : 알파벳이나 숫자
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);   // get a matcher object
        while (matcher.find()) {
            String url = matcher.group();
            //Log.e(mTag, "url: " + url);

            String id = matcher.group(1);
            String thumbnail = "https://img.youtube.com/vi/" + id + "/0.jpg";
            //Log.e(mTag, "thumbnail: " + thumbnail);

            addMediaData(mediaDatas, thumbnail, null, url);
        }

        for (Element iframe : root.select("iframe")) {
            String url = iframe.attr("src");
            //Log.e(mTag, "url: " + url);

            regex = "https\\://www\\.youtube\\.com/embed/([_|\\w]+)"; // \\w : 알파벳이나 숫자
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(url);   // get a matcher object
            while (matcher.find()) {
                String id = matcher.group(1);
                String thumbnail = "https://img.youtube.com/vi/" + id + "/0.jpg";
                //Log.e(mTag, "thumbnail: " + thumbnail);

                addMediaData(mediaDatas, thumbnail, null, url);
            }
        }
    }

    protected void addMediaData(ArrayList<MediaData> mediaDatas, String thumbnail, String image, String url) {
        MediaData mediaData = new MediaData();
        mediaData.setThumbnail(thumbnail);
        mediaData.setImage(image);
        mediaData.setUrl(url);

        mediaDatas.add(mediaData);
    }
}