package com.summertaker.community.parser;

import com.summertaker.community.common.BaseParser;
import com.summertaker.community.data.MediaData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TwitterParser extends BaseParser {

    public MediaData getMediaData(String response) {
        /*
        <div class="card-photo">
            <div class="media">
                <img src="https://pbs.twimg.com/media/DNHUYrzUEAEiH4x.jpg:small"/>
            </div>
        */

        MediaData mediaData = new MediaData();

        Document doc = Jsoup.parse(response);

        Element root = doc.select(".card-photo").first();
        if (root != null) {
            Element el = root.select(".media").first();
            el = el.select("img").first();
            String thumbnail = el.attr("src");
            String image = thumbnail.replace(":small", "");

            mediaData.setThumbnail(thumbnail);
            mediaData.setImage(image);
        }

        return mediaData;
    }
}
