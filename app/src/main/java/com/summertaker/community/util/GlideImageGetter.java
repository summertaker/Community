package com.summertaker.community.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.summertaker.community.R;

/*
 * http://codegists.com/snippet/java/glideimagegetterjava_akyuu_java
 */
public class GlideImageGetter implements Html.ImageGetter{
    private final Context mContext;
    private final TextView mTextView;
    private final UrlDrawable mUrlDrawable;

    public GlideImageGetter(Context context, TextView textView) {
        mContext = context;
        mTextView = textView;
        mUrlDrawable = new UrlDrawable();
    }

    @Override
    public Drawable getDrawable(String url) {
        //Log.e("GLIDE", "URL: " + url);
        Glide.with(mContext)
                .load(url)
                //.placeholder(R.mipmap.ic_error)
                //.error(R.mipmap.ic_error)
                .into(new ViewTarget<TextView, Drawable>(mTextView) {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        Rect rect = new Rect(0, 0, resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                        resource.setBounds(rect);
                        mUrlDrawable.setDrawable(resource);
                        mUrlDrawable.setBounds(rect);
                        mTextView.setText(mTextView.getText());
                    }
                });
        return mUrlDrawable;
    }

    class UrlDrawable extends Drawable {
        private Drawable mDrawable;

        public void setDrawable(Drawable drawable) {
            mDrawable = drawable;
        }

        @Override
        public void draw(@android.support.annotation.NonNull Canvas canvas) {
            if (mDrawable != null) {
                mDrawable.draw(canvas);
                if (mDrawable instanceof GifDrawable) {
                    GifDrawable gif = (GifDrawable) mDrawable;
                    gif.setLoopCount(GifDrawable.LOOP_FOREVER);
                    gif.start();
                }
                mTextView.invalidate();
            }
        }

        @Override
        public void setAlpha(@IntRange(from = 0, to = 255) int i) {
            if (mDrawable != null) {
                mDrawable.setAlpha(i);
            }
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            if (mDrawable != null) {
                mDrawable.setColorFilter(colorFilter);
            }
        }

        @Override
        public int getOpacity() {
            if (mDrawable != null) {
                return mDrawable.getOpacity();
            } else {
                return PixelFormat.UNKNOWN;
            }
        }
    }
}
