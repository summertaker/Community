package com.summertaker.community.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.summertaker.community.R;

public class PicassoImageGetter implements Html.ImageGetter {

    private Context mContext;
    private TextView mTextView = null;

    public PicassoImageGetter() {

    }

    public PicassoImageGetter(Context context, TextView target) {
        mContext = context;
        mTextView = target;
    }

    @Override
    public Drawable getDrawable(String source) {
        BitmapDrawablePlaceHolder drawable = new BitmapDrawablePlaceHolder();
        Picasso.with(mContext)
                .load(source)
                .placeholder(R.drawable.placeholder)
                .into(drawable);
        return drawable;
    }

    private class BitmapDrawablePlaceHolder extends BitmapDrawable implements Target {

        protected Drawable drawable;

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;

            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();

            /*
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            width = (int) dpWidth;
            */

            /*
            int margin = (int) mContext.getResources().getDimension(R.dimen.activity_margin);
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x - (margin * 2);
            height = size.y;
            */

            drawable.setBounds(0, 0, width, height);
            setBounds(0, 0, width, height);
            if (mTextView != null) {
                mTextView.setText(mTextView.getText());
            }
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Log.e("", "onBitmapLoaded()....");
            setDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.e("", "PicassoImageGetter.onBitmapFailed()....");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

    }
}