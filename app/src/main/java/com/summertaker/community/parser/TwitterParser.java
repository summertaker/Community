package com.summertaker.community.parser;

import android.util.Log;

import com.summertaker.community.common.BaseParser;
import com.summertaker.community.data.MediaData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TwitterParser extends BaseParser {

    public MediaData getMediaData(String response) {
        /*
        <td class="user-info">
          <div class="fullname">
            <a href="/nhk_segodon?p=s">
              <strong>大河ドラマ「西郷どん」</strong>
            </a>
          </div>
          <a class="user-info-username" href="/nhk_segodon?p=s">
            <span class="username">
              <span>@</span>nhk_segodon
            </span>
          </a>
        </td>
        <div class="card-photo">
            <div class="media">
                <img src="https://pbs.twimg.com/media/DNHUYrzUEAEiH4x.jpg:small"/>
            </div>
        */

        MediaData mediaData = new MediaData();

        Document doc = Jsoup.parse(response);

        Element root;

        root = doc.select(".main-tweet").first();

        if (root != null) {
            String html = root.outerHtml();

            String avatar = root.select(".avatar").first().html();
            html = html.replace(avatar, "");

            mediaData.setHtml(html);

            String name = root.select(".fullname").first().text();
            //Log.e(mTag, "name: " + name);
            mediaData.setName(name);
        }

        root = doc.select(".tweet-content").first();
        if (root != null) {
            String description = root.select(".tweet-text").first().text();
            //Log.e(mTag, "description: " + description);
            mediaData.setDescription(description);

            String datetime = root.select(".metadata").first().text();
            //Log.e(mTag, "datetime: " + datetime);
            mediaData.setDatetime(datetime);
        }

        root = doc.select(".card-photo").first();
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
