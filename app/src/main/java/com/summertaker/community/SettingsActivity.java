package com.summertaker.community;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.summertaker.community.common.BaseActivity;
import com.summertaker.community.common.BaseApplication;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init the swipe back
        SwipeBack.attach(this, Position.LEFT)
                .setContentView(R.layout.settings_activity)
                .setSwipeBackView(R.layout.swipeback_default);
        //setContentView(R.layout.settings_activity);

        setBaseStatusBar(); // 상태바 설정
        setBaseToolbar(getString(R.string.action_settings)); // 툴바 설정

        CheckBox cbUseImageGetter = findViewById(R.id.cb_use_image_getter);
        cbUseImageGetter.setChecked(BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER);
    }

    public void onUseImageGetterClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.cb_use_image_getter:
                BaseApplication.getInstance().SETTINGS_USE_IMAGE_GETTER = checked;
                break;
        }

        BaseApplication.getInstance().saveSettings();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.swipeback_stack_to_front, R.anim.swipeback_stack_right_out);
    }
}
