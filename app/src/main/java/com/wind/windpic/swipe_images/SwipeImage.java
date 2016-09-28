package com.wind.windpic.swipe_images;

import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.CameraFragment;
import com.wind.windpic.fragments.FriendsFragment;
import com.wind.windpic.fragments.PhotosFragment;
import com.wind.windpic.tools.Methods;


/**
 * Created by dianapislaru on 18/08/15.
 */
public class SwipeImage implements View.OnTouchListener, View.OnClickListener {

    public static final String TAG = "SwipeImage";

    private Activity mActivity;

    public static RelativeLayout mFront;
    public static ImageView mFrontImage;
    public static ImageView mBackImage;
    public static ImageView mYesImage;
    public static ImageView mNoImage;
    private static TextView mNoPicturesTextView;
    public static ViewGroup mContainer;
    public ImageButton mYesButton;
    public ImageButton mNoButton;
    public static LinearLayout mButtonsLayout;
    public SwipeAnimation mSwipeAnimation;
    private ButtonSwipeAnimation mButtonSwipeAnimation;
    public static TextView mUserNameTextView;
    public static TextView mUserInfoTextView;

    public int displayWidth;
    public int displayHeight;
    private int touchViewX;
    private int touchViewY;
    private int touchX;
    private int touchY;
    private int initialPositionX;
    private int initialPositionY;
    public int screenCenterX;
    private int screenCenterY;
    public int imageWidth;
    public int imageHeight;
    public int maxDelta;
    private int actionBarHeight;
    private int topBottomDelta;

    private RelativeLayout.LayoutParams params;

    public SwipeImage(Activity activity, View view) {
        mActivity = activity;

        mContainer = (RelativeLayout) view.findViewById(R.id.container);
        mFront = (RelativeLayout) view.findViewById(R.id.front);
        mFrontImage = (ImageView) view.findViewById(R.id.front_image);
        mBackImage = (ImageView) view.findViewById(R.id.back_image);
        mYesImage = (ImageView) view.findViewById(R.id.yes_image);
        mNoImage = (ImageView) view.findViewById(R.id.no_image);
        mYesButton = (ImageButton) view.findViewById(R.id.yes_button);
        mNoButton = (ImageButton) view.findViewById(R.id.no_button);
        mButtonsLayout = (LinearLayout) view.findViewById(R.id.buttons_layout);
        mNoPicturesTextView = (TextView) view.findViewById(R.id.fragment_photos_no_pictures_textView);
        mUserNameTextView = (TextView) view.findViewById(R.id.fragment_photos_user_name_textView);
        mUserInfoTextView = (TextView) view.findViewById(R.id.fragment_photos_user_info_textView);

        mYesButton.setColorFilter(mActivity.getResources().getColor(R.color.dark));
        mNoButton.setColorFilter(mActivity.getResources().getColor(R.color.dark));
        mYesImage.setColorFilter(mActivity.getResources().getColor(R.color.dark));
        mNoImage.setColorFilter(mActivity.getResources().getColor(R.color.dark));
    }

    public void show() {
        if (PhotosFragment.mFrontPhoto != null) {
            initValues();
            initFront();
            if (PhotosFragment.mBackPhoto != null) {
                initBack();
            }
            showButtons();
            initButtons();
            mSwipeAnimation = new SwipeAnimation(mActivity, displayWidth, initialPositionX, initialPositionY, imageWidth, imageHeight, topBottomDelta);
            mButtonSwipeAnimation = new ButtonSwipeAnimation(displayWidth, initialPositionX, initialPositionY, imageWidth, imageHeight, topBottomDelta);
        } else {
            initNoPicturesTextView(false);
            removeButtons();
        }

    }

    private void initButtons() {
        mYesButton.setOnClickListener(this);
        mNoButton.setOnClickListener(this);
    }

    private void removeButtons() {
        mButtonsLayout.setVisibility(View.GONE);
    }

    private void showButtons() {
        mButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void initValues() {
        actionBarHeight = Methods.getActionBarHeight(mActivity);
        Point display = Methods.getSmallDisplaySize(mActivity);
        displayWidth = display.x;
        displayHeight = display.y -= 2 * actionBarHeight;
        imageWidth = (int) (displayWidth * 0.85);
        imageHeight = (int) (displayHeight * 0.9);
        screenCenterX = displayWidth / 2;
        screenCenterY = displayHeight / 2;
        initialPositionX = screenCenterX - (imageWidth / 2);
        initialPositionY = screenCenterY - (imageHeight / 2) + actionBarHeight;
        maxDelta = (int) (displayHeight * 0.25);
        topBottomDelta = imageHeight / 40;
    }

    private void initFront() {
        if (PhotosFragment.mFrontPhoto == null)
            return;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
        layoutParams.topMargin = initialPositionY;
        layoutParams.leftMargin = initialPositionX;
        mFront.setLayoutParams(layoutParams);
        mFront.setRotation(0);
        mFrontImage.setImageBitmap(PhotosFragment.mFrontPhoto.getPhoto());
        mUserNameTextView.setText(PhotosFragment.mFrontPhoto.getFirstName() + ", "
                + PhotosFragment.mFrontPhoto.getAge());
        int distance = PhotosFragment.mFrontPhoto.getDistance();
        Log.i(TAG, distance + "MILE");
        if (distance > 0) {
            mUserInfoTextView.setText(distance + " miles away");
        } else {
            mUserInfoTextView.setText("less than a mile away");
        }

        mFront.setOnTouchListener(this);

        mYesImage.setAlpha(0);
        mNoImage.setAlpha(0);

        mNoPicturesTextView.setVisibility(View.INVISIBLE);
    }

    public void initBack() {
        if (PhotosFragment.mBackPhoto == null)
            return;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
        layoutParams.topMargin = initialPositionY + topBottomDelta;
        layoutParams.leftMargin = initialPositionX;
        mBackImage.setLayoutParams(layoutParams);
        mBackImage.setImageBitmap(PhotosFragment.mBackPhoto.getPhoto());
    }

    public static void removeFront() {
        mButtonsLayout.setAnimation(fadeOutAnimation());
        mNoPicturesTextView.setAnimation(fadeInAnimation());

        // Photos Notifications = false
        WindpicApplication.PHOTOS = false;
        hidePhotosNotificationIcon();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(0, 0);
        mFront.setLayoutParams(layoutParams);
        mFrontImage.setImageBitmap(null);
        mUserNameTextView.setText("");
        mUserInfoTextView.setText("");

        mFront.setOnTouchListener(null);

        mYesImage.setAlpha(0);
        mNoImage.setAlpha(0);
    }

    private void cancelSwipe() {
        // Resume Back position
        if (PhotosFragment.mBackPhoto != null) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
            layoutParams.topMargin = initialPositionY + topBottomDelta;
            layoutParams.leftMargin = initialPositionX;
            mBackImage.setLayoutParams(layoutParams);
        }

        // Resume Front position
        params = (RelativeLayout.LayoutParams) mFront.getLayoutParams();
        params.leftMargin = initialPositionX;
        params.topMargin = initialPositionY;
        mFront.setRotation(0);
        mFront.setLayoutParams(params);
    }

    private void showYesNoImages(int value) {
        if (value > 0) {
            if (value / 2 <= 255) {
                mYesImage.setAlpha(value / 2);
                mNoImage.setAlpha(0);
            } else {
                mYesImage.setAlpha(255);
            }
        } else {
            if (Math.abs(value / 2) <= 255) {
                mNoImage.setAlpha(Math.abs(value / 2));
                mYesImage.setAlpha(0);
            } else {
                mNoImage.setAlpha(255);
            }
        }
    }

    private void elevateBack(int value) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBackImage.getLayoutParams();

        if (Math.abs(value) > maxDelta) {
            params.topMargin = initialPositionY;
        } else {
            value = Math.abs(value / 10);
            if (initialPositionY + topBottomDelta - value > initialPositionY && initialPositionY + topBottomDelta - value < initialPositionY + topBottomDelta) {
                params.topMargin = initialPositionY + topBottomDelta - value;
            }
        }
        mBackImage.setLayoutParams(params);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        final int newViewCenterX = screenCenterX + (X - touchX);
        final int newViewCenterY = screenCenterY + (Y - touchY);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // Get the touch position
                params = (RelativeLayout.LayoutParams) mFront.getLayoutParams();
                touchViewX = X - params.leftMargin;
                touchViewY = Y - params.topMargin;
                touchX = X;
                touchY = Y;
                break;
            case MotionEvent.ACTION_UP:
                // Hide the Yes/No Images
                mYesImage.setAlpha(0);
                mNoImage.setAlpha(0);

                if (Math.abs(screenCenterX - newViewCenterX) > maxDelta) {

                    // SWIPE
                    if (newViewCenterX - screenCenterX < 0) {
                        mSwipeAnimation.swipeLeft();
                    } else {
                        mSwipeAnimation.swipeRight();
                    }

                } else {
                    // CANCEL SWIPE
                    cancelSwipe();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                params = (RelativeLayout.LayoutParams) mFront.getLayoutParams();
                int deltaX = X - touchViewX;
                int deltaY = Y - touchViewY;
                int delta = newViewCenterX - screenCenterX;

                params.leftMargin = deltaX;
                params.topMargin = deltaY;
                params.rightMargin = displayWidth - (deltaX - mFront.getWidth());
                params.bottomMargin = displayHeight - (deltaY - mFront.getHeight());
                mFront.setLayoutParams(params);

                mFront.setRotation(delta / 50);

                showYesNoImages(delta);
                elevateBack(delta);

                break;
        }
        mContainer.invalidate();
        return true;
    }

    public static AnimationSet fadeInAnimation() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mNoPicturesTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return animation;
    }

    public static AnimationSet fadeOutAnimation() {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(1000);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeOut);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mButtonsLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return animation;
    }

    private static void initNoPicturesTextView(boolean isPicture) {
        if (isPicture) {
            mNoPicturesTextView.setVisibility(View.INVISIBLE);
        } else {
            mNoPicturesTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        if (!Methods.isInternetAvailable(mActivity)) {
            Toast.makeText(mActivity, "No internet connection!", Toast.LENGTH_LONG).show();
            return;
        }
        if (view == mYesButton) {
            mButtonSwipeAnimation.swipeRight();
        } else {
            mButtonSwipeAnimation.swipeLeft();
        }
    }

    public static void hidePhotosNotificationIcon() {
        CameraFragment.updatePhotosNotifications(false);
        FriendsFragment.updatePhotosNotifications(false);
    }

}
