package com.summertaker.community.data;

import java.util.ArrayList;

public class ArticleDetailData {
    private String title;
    private String name;
    private String date;
    private String url;
    private String content;
    private ArrayList<String> thumbnails;
    private ArrayList<String> images;
    private String parentTable;
    private String parentId;

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

    public ArrayList<String> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(ArrayList<String> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getParentTable() {
        return parentTable;
    }

    public void setParentTable(String parentTable) {
        this.parentTable = parentTable;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
