package com.summertaker.community.article;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Picasso;
import com.summertaker.community.R;
import com.summertaker.community.common.BaseApplication;
import com.summertaker.community.common.BaseDataAdapter;
import com.summertaker.community.common.ImageViewActivity;
import com.summertaker.community.data.CommentData;
import com.summertaker.community.util.ImageUtil;

import java.util.ArrayList;

public class ArticleCommentAdapter extends BaseDataAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<CommentData> mDataList = null;
    private Resources mResources;

    private ArticleViewInterface mArticleViewInterface;

    public ArticleCommentAdapter(Context context, ArrayList<CommentData> dataList, ArticleViewInterface articleViewInterface) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mDataList = dataList;
        this.mArticleViewInterface = articleViewInterface;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ArticleCommentAdapter.ViewHolder holder;
        final CommentData commentData = mDataList.get(position);

        if (convertView == null) {
            holder = new ArticleCommentAdapter.ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.article_comment_item, null);
            holder.loComment = convertView.findViewById(R.id.loComment);
            holder.ivContent = convertView.findViewById(R.id.ivPicture);
            holder.tvContent = convertView.findViewById(R.id.tvContent);

            convertView.setTag(holder);
        } else {
            holder = (ArticleCommentAdapter.ViewHolder) convertView.getTag();
        }

        if (BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER) {
            // https://medium.com/@rajeefmk/android-textview-and-image-loading-from-url-part-1-a7457846abb6
            Spannable html = ImageUtil.getSpannableHtmlWithImageGetter(mContext, holder.tvContent, commentData.getContent());
            ImageUtil.setClickListenerOnHtmlImageGetter(html, new ImageUtil.Callback() {
                @Override
                public void onImageClick(String imageUrl) {
                    //Log.e(mTag, "imageUrl: " + imageUrl);
                    Intent intent = new Intent(mContext, ImageViewActivity.class);
                    intent.putExtra("url", imageUrl);
                    mContext.startActivity(intent);
                }
            }, true);
            holder.tvContent.setText(html);
            holder.tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            // 이미지와 비디오 썸네일을 직접 처리하는 경우
            final String thumbnail = commentData.getThumbnail();

            if (thumbnail == null || thumbnail.isEmpty()) {
                holder.ivContent.setVisibility(View.GONE);
            } else {
                holder.ivContent.setVisibility(View.VISIBLE);

                if (thumbnail.toLowerCase().contains(".gif")) {
                    Glide.with(mContext).asGif().load(thumbnail).into(holder.ivContent);
                } else {
                    Picasso.with(mContext).load(thumbnail).placeholder(R.drawable.placeholder).into(holder.ivContent);
                }

                holder.ivContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mArticleViewInterface.onPictureClick(position);
                    }
                });
            }

            holder.tvContent.setText(commentData.getContent());
        }

        //holder.tvContent.setTextColor(mResources.getColor(R.color.comment_text_color));
        //holder.loComment.setBackgroundColor(mResources.getColor(R.color.white));

        //if (commentData.isReply()) {
        //    holder.tvContent.setPadding(100, 0, 0, 0);
        //} else {
        //    holder.tvContent.setPadding(0, 0, 0, 0);
        //}

        //if (commentData.isBest()) {
        //    //holder.tvContent.setTextColor(mResources.getColor(R.color.best_comment_text_color));
        //    holder.loComment.setBackgroundColor(mResources.getColor(R.color.best_comment_background_color));
        //}

        return convertView;
    }

    static class ViewHolder {
        LinearLayout loComment;
        ImageView ivContent;
        TextView tvContent;
    }
}

