package com.summertaker.community.parser;

import android.util.Log;

import com.summertaker.community.common.BaseParser;
import com.summertaker.community.data.MediaData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class InstagramParser extends BaseParser {

    public MediaData getMediaData(String response) {
        /*
        <meta property="og:image" content="https://scontent-icn1-1.cdninstagram.com/t51.2885-15/e35/22860543_1652982868067029_5950211781656838144_n.jpg?se=7" />
        */

        MediaData mediaData = new MediaData();

        Document doc = Jsoup.parse(response);

        for (Element meta : doc.select("meta")) {
            //Log.e(mTag, "meta.outerHtml(): " + meta.outerHtml());

            String property = meta.attr("property");

            if ("og:image".equals(property)) {
                String thumbnail = meta.attr("content");
                //Log.e(mTag, "thumbnail: " + thumbnail);

                mediaData.setThumbnail(thumbnail);
                mediaData.setImage(thumbnail);
                break;
            }
        }

        return mediaData;
    }
}