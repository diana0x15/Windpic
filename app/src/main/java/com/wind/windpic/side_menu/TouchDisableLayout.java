package com.wind.windpic.side_menu;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by dianapislaru on 13/08/15.
 */
public class TouchDisableLayout extends RelativeLayout {

    private boolean mTouchDisabled = false;

    private View mContent;

    public TouchDisableLayout(Context context) {
        super(context);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mTouchDisabled;
    }

    void setTouchDisable(boolean disableTouch) {
        mTouchDisabled = disableTouch;
    }

    boolean isTouchDisabled() {
        return mTouchDisabled;
    }
}
