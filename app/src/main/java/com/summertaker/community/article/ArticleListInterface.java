package com.summertaker.community.article;

public interface ArticleListInterface {

    void onPictureClick(int position, String imageUrl);

    void onTitleClick(int position);

    void onCloseClick(int position);
}
