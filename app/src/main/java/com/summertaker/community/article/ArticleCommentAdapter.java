package com.summertaker.community.article;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.summertaker.community.R;
import com.summertaker.community.common.BaseDataAdapter;
import com.summertaker.community.data.CommentData;

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

        final String thumbnail = commentData.getThumbnail();
        if (thumbnail == null || thumbnail.isEmpty()) {
            holder.ivContent.setVisibility(View.GONE);
        } else {
            holder.ivContent.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(thumbnail).placeholder(R.drawable.placeholder).into(holder.ivContent);

            holder.ivContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mArticleViewInterface.onPictureClick(position);
                }
            });
        }

        holder.tvContent.setText(commentData.getContent());
        //holder.tvContent.setTextColor(mResources.getColor(R.color.comment_text_color));
        holder.loComment.setBackgroundColor(mResources.getColor(R.color.white));

        if (commentData.isReply()) {
            holder.tvContent.setPadding(100, 0, 0, 0);
        } else {
            holder.tvContent.setPadding(0, 0, 0, 0);
        }

        if (commentData.isBest()) {
            //holder.tvContent.setTextColor(mResources.getColor(R.color.best_comment_text_color));
            holder.loComment.setBackgroundColor(mResources.getColor(R.color.best_comment_background_color));
        }

        return convertView;
    }

    static class ViewHolder {
        LinearLayout loComment;
        ImageView ivContent;
        TextView tvContent;
    }
}

