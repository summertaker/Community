package com.summertaker.community.data;

public class CommentData {

    private String thumbnail;
    private String image;
    private String content;
    private boolean isBest;
    private boolean isReply;

    public CommentData() {

    }

    public CommentData(String thumbnail, String image, String content) {
        this.thumbnail = thumbnail;
        this.image = image;
        this.content = content;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isBest() {
        return isBest;
    }

    public void setBest(boolean best) {
        isBest = best;
    }

    public boolean isReply() {
        return isReply;
    }

    public void setReply(boolean reply) {
        isReply = reply;
    }
}
