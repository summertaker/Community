package com.summertaker.community.data;

import java.util.ArrayList;

public class ArticleDetailData {
    private String title;
    private String name;
    private String date;
    private String url;
    private String content;
    private ArrayList<MediaData> mediaDatas;
    private ArrayList<String> outLinks;
    private String table;
    private String id;

    public ArticleDetailData() {
    }

    public ArticleDetailData(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<MediaData> getMediaDatas() {
        return mediaDatas;
    }

    public void setMediaDatas(ArrayList<MediaData> mediaDatas) {
        this.mediaDatas = mediaDatas;
    }

    public ArrayList<String> getOutLinks() {
        return outLinks;
    }

    public void setOutLinks(ArrayList<String> outLinks) {
        this.outLinks = outLinks;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
