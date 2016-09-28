package com.wind.windpic.swipe_images;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wind.windpic.MainActivity;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.PhotosFragment;
import com.wind.windpic.objects.Photo;

/**
 * Created by dianapislaru on 18/08/15.
 */
public class ButtonSwipeAnimation implements Animator.AnimatorListener {

    public static final String TAG = "ButtonSwipeAnimation";

    private View mView;
    private ImageView mViewImage;
    private ImageView mBackView;
    private ImageView mYes;
    private ImageView mNo;
    private TextView mUserInfoTextView;
    private TextView mUserNameTextView;
    private int mTarget;
    private int mInitialPositionX;
    private int mInitialPositionY;
    private int mViewWidth;
    private int mViewHeight;
    private int mTopBottomDelta;

    public ButtonSwipeAnimation(int target, int initialPositionX, int initialPositionY, int viewWidth, int viewHeight, int delta) {
        mView = SwipeImage.mFront;
        mViewImage = SwipeImage.mFrontImage;
        mBackView = SwipeImage.mBackImage;
        mYes = SwipeImage.mYesImage;
        mNo = SwipeImage.mNoImage;
        mUserInfoTextView = SwipeImage.mUserInfoTextView;
        mUserNameTextView = SwipeImage.mUserNameTextView;
        mTarget = target;
        mInitialPositionX = initialPositionX;
        mInitialPositionY = initialPositionY;
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
        mTopBottomDelta = delta;
    }

    public void swipeRight() {
        mYes.setAlpha(200);
        AnimatorSet swipe = new AnimatorSet();
        swipe.playTogether(ObjectAnimator.ofFloat(mView, "translationX", 0, 2 * mTarget),
                ObjectAnimator.ofFloat(mView, "rotation", 0, 20),
                ObjectAnimator.ofFloat(mBackView, "translationY", 0, -1 * mTopBottomDelta));
        swipe.setDuration(600);
        swipe.addListener(this);
        swipe.start();

        // Send Friend Request
        sendFriendRequest(PhotosFragment.mFrontPhoto);
    }

    public void swipeLeft() {
        mNo.setAlpha(200);
        AnimatorSet swipe = new AnimatorSet();
        swipe.playTogether(ObjectAnimator.ofFloat(mView, "translationX", 0, -2 * mTarget),
                ObjectAnimator.ofFloat(mView, "rotation", 0, -20),
                ObjectAnimator.ofFloat(mBackView, "translationY", 0, -1 * mTopBottomDelta));
        swipe.setDuration(600);
        swipe.addListener(this);
        swipe.start();
    }


    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mView.clearAnimation();
        mView.setRotation(0);
        mView.setTranslationX(0);
        mBackView.setTranslationY(0);
        mYes.setAlpha(0);
        mNo.setAlpha(0);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mViewWidth, mViewHeight);
        params.leftMargin = mInitialPositionX;
        params.topMargin = mInitialPositionY;
        mView.setLayoutParams(params);

        // Resume Back position
        params = (RelativeLayout.LayoutParams) mBackView.getLayoutParams();
        params.topMargin = mInitialPositionY + mTopBottomDelta;
        mBackView.setLayoutParams(params);

        // unpin mView image, change mView image, fade in mBackView

        // DELETE PHOTO
        PhotosFragment.mFrontPhoto.getObject().unpinInBackground(MainActivity.CURRENT_USER.getObjectId());
        PhotosFragment.mFrontPhoto = PhotosFragment.mBackPhoto;

        if (WindpicApplication.PHOTOS_LIST.size() > 0) {
            WindpicApplication.PHOTOS_LIST.remove(0);
        }
        if (WindpicApplication.PHOTOS_LIST.size() > 0) {
            PhotosFragment.mBackPhoto = WindpicApplication.PHOTOS_LIST.get(0);
        } else {
            PhotosFragment.mBackPhoto = null;
        }

        if (PhotosFragment.mFrontPhoto != null) {
            mViewImage.setImageBitmap(PhotosFragment.mFrontPhoto.getPhoto());
            mUserNameTextView.setText(PhotosFragment.mFrontPhoto.getFirstName() + ", "
                    + PhotosFragment.mFrontPhoto.getAge());
            mUserInfoTextView.setText(PhotosFragment.mFrontPhoto.getDistance() + " miles away");

            if (PhotosFragment.mBackPhoto == null) {
                mBackView.setVisibility(View.GONE);
            } else {
                // Fade in BackView
                mBackView.setImageBitmap(PhotosFragment.mBackPhoto.getPhoto());
                AnimatorSet fade = new AnimatorSet();
                fade.playTogether(ObjectAnimator.ofFloat(mBackView, "alpha", 0f, 1f));
                fade.setDuration(400);
                fade.start();
            }
        } else {
            SwipeImage.removeFront();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    private void sendFriendRequest(Photo photo) {
        String sender = MainActivity.CURRENT_USER.getObjectId();
        String receiver = photo.getAuthor();
        WindpicApplication.FIREBASE.child("PENDING_FRIEND_REQUESTS").child(receiver).child(sender).setValue(sender);
    }

}
