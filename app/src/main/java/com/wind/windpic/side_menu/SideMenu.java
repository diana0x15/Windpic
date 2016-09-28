package com.wind.windpic.side_menu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.wind.windpic.R;
import com.wind.windpic.MainActivity;
import com.wind.windpic.tools.Methods;

import java.util.ArrayList;

public class SideMenu extends FrameLayout {

    private Context mContext;
    private Activity mActivity;
    private ArrayList<SideMenuItem> mMenuItems;
    private TouchDisableLayout mTouchDisableLayout;
    private ViewGroup mViewDecor;
    private SideMenuListener mMenuListener;

    private ImageView mBackgroundImageView;
    private ImageView mShadowImageView;
    private ScrollView mScrollView;
    private LinearLayout mLayout;

    private boolean isOpened = false;
    private float mShadowAdjustScaleX;
    private float mShadowAdjustScaleY;
    private float mScaleValue = 0.7f;

    public SideMenu(Context context) {
        super(context);
        mContext = context;
        initViews(context);
    }

    public void attachToActivity(Activity activity) {
        initValues(activity);
        setShadowAdjustScaleX();
        mViewDecor.addView(this, 0);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.side_menu, this);
        mBackgroundImageView = (ImageView) findViewById(R.id.side_menu_background);
        mShadowImageView = (ImageView) findViewById(R.id.side_menu_shadow);
        mScrollView = (ScrollView) findViewById(R.id.side_menu_scrollView);
        mLayout = (LinearLayout) findViewById(R.id.side_menu_layout);
    }

    private void initValues(Activity activity) {
        mActivity = activity;
        mMenuItems = new ArrayList<SideMenuItem>();
        mViewDecor = (ViewGroup) mActivity.getWindow().getDecorView();

        mTouchDisableLayout = new TouchDisableLayout(mActivity);

        View content = mViewDecor.getChildAt(0);
        mViewDecor.removeViewAt(0);
        mTouchDisableLayout.addView(content);
        addView(mTouchDisableLayout);

        ViewGroup parent = (ViewGroup) mScrollView.getParent();
        parent.removeView(mScrollView);
    }

    private void setShadowAdjustScaleX() {
        mShadowAdjustScaleX = 0.06f;
        mShadowAdjustScaleY = 0.07f;
    }

    // Set the background color
    public void setBackgroundColor(int color) {
        mBackgroundImageView.setBackgroundColor(color);
    }

    // Set background resource
    public void setBackgroundImage(Drawable image) {
        mBackgroundImageView.setImageDrawable(image);
    }

    // Set the visibility of the shadow under the activity
    public void setShadowVisible(boolean isVisible) {
        if (isVisible) {
            mShadowImageView.setBackgroundResource(R.drawable.shadow);
        } else {
            mShadowImageView.setBackgroundResource(0);
        }
    }

    // Add item to the menu
    public void addItem(SideMenuItem item) {
        mMenuItems.add(item);
        mLayout.addView(item);
    }

    private void rebuildMenu() {
        mLayout.removeAllViews();
        for (SideMenuItem item : mMenuItems) {
            mLayout.addView(item);
        }
    }

    private ArrayList<SideMenuItem> getSideMenuItems() {
        return mMenuItems;
    }

    public void setListener(SideMenuListener listener) {
        mMenuListener = listener;
    }

    public SideMenuListener getListener() {
        return mMenuListener;
    }

    // Show the menu
    public void open() {
        isOpened = true;
        AnimatorSet activityAnimator = buildScaleDownAnimator(mTouchDisableLayout, mScaleValue, mScaleValue, getScreenWidth() * 0.3f);
        AnimatorSet shadowAnimator = buildScaleDownAnimator(mShadowImageView, mScaleValue + mShadowAdjustScaleX, mScaleValue + mShadowAdjustScaleY, getScreenWidth() * 0.3f);
        AnimatorSet menuAnimator = buildMenuAnimator(mScrollView, 1.0f);
        activityAnimator.addListener(animationListener);
        activityAnimator.playTogether(shadowAnimator);
        activityAnimator.playTogether(menuAnimator);
        activityAnimator.start();

        Methods.disableFullScreen(mActivity);
    }

    // Hide the menu
    public void close() {
        isOpened = false;
        AnimatorSet activityAnimator = buildScaleUpAnimator(mTouchDisableLayout, 1.0f, 1.0f);
        AnimatorSet shadowAnimator = buildScaleUpAnimator(mTouchDisableLayout, 1.0f, 1.0f);
        AnimatorSet menuAnimator = buildMenuAnimator(mScrollView, 0.0f);
        activityAnimator.addListener(animationListener);
        activityAnimator.playTogether(shadowAnimator);
        activityAnimator.playTogether(menuAnimator);
        activityAnimator.start();

        if (MainActivity.FRAGMENT_CAMERA) {
            Methods.enableFullScreen(mActivity);
        } else {
            Methods.disableFullScreen(mActivity);
        }
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setScaleValue(float scaleValue) {
        mScaleValue = scaleValue;
    }

    public AnimatorSet buildScaleDownAnimator(View view, float targetScaleX, float targetScaleY, float positionX) {

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(ObjectAnimator.ofFloat(view, "translationX", 0, positionX),
                ObjectAnimator.ofFloat(view, "scaleX", targetScaleX),
                ObjectAnimator.ofFloat(view, "scaleY", targetScaleY), ObjectAnimator.ofFloat(mScrollView, "alpha", 1.0f));
        scaleDown.setDuration(250);
        return scaleDown;
    }

    public AnimatorSet buildScaleUpAnimator(View view, float targetScaleX, float targetScaleY) {

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(ObjectAnimator.ofFloat(view, "translationX", 0),
                ObjectAnimator.ofFloat(view, "scaleX", targetScaleX),
                ObjectAnimator.ofFloat(view, "scaleY", targetScaleY));
        scaleUp.setDuration(250);
        return scaleUp;
    }

    public AnimatorSet buildMenuAnimator(View view, float alpha) {
        AnimatorSet menuAnimator = new AnimatorSet();
        menuAnimator.playTogether(ObjectAnimator.ofFloat(view, "alpha", alpha));
        menuAnimator.setDuration(500);
        return menuAnimator;
    }

    public int getScreenWidth() {
        return Methods.getSmallDisplaySize(mActivity).x;
    }

    public interface SideMenuListener {
        void openMenu();
        void closeMenu();
    }

    private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (isOpened()){
                showScrollViewMenu(mScrollView);
                setShadowVisible(true);
                if (mMenuListener != null)
                    mMenuListener.openMenu();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // reset the view;
            if(isOpened()){
                mTouchDisableLayout.setTouchDisable(true);
                mTouchDisableLayout.setOnClickListener(viewActivityOnClickListener);
            } else {
                mTouchDisableLayout.setTouchDisable(false);
                mTouchDisableLayout.setOnClickListener(null);
                hideScrollViewMenu(mScrollView);
                setShadowVisible(false);
                if (mMenuListener != null)
                    mMenuListener.closeMenu();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    private OnClickListener viewActivityOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isOpened()) {
                close();
            }
        }
    };

    private void showScrollViewMenu(ScrollView scrollViewMenu){
        if (scrollViewMenu != null && scrollViewMenu.getParent() == null){
            addView(scrollViewMenu);
        }
    }

    private void hideScrollViewMenu(ScrollView scrollViewMenu){
        if (scrollViewMenu != null && scrollViewMenu.getParent() != null){
            removeView(scrollViewMenu);
        }
    }

}