package com.wind.windpic.swipe_images;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wind.windpic.MainActivity;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.PhotosFragment;
import com.wind.windpic.objects.Photo;
import com.wind.windpic.tools.Methods;

/**
 * Created by dianapislaru on 18/08/15.
 */
public class SwipeAnimation implements Animator.AnimatorListener {

    public static final String TAG = "SwipeAnimation";

    private Context mContext;
    private View mView;
    private ImageView mViewImage;
    private ImageView mBackView;
    private TextView mUserInfoTextView;
    private TextView mUserNameTextView;
    private int mTarget;
    private int mInitialPositionX;
    private int mInitialPositionY;
    private int mViewWidth;
    private int mViewHeight;
    private int mTopBottomDelta;

    public SwipeAnimation(Context context, int target, int initialPositionX, int initialPositionY, int viewWidth, int viewHeight, int delta) {
        mContext = context;
        mView = SwipeImage.mFront;
        mViewImage = SwipeImage.mFrontImage;
        mBackView = SwipeImage.mBackImage;
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
        AnimatorSet swipe = new AnimatorSet();
        swipe.playTogether(ObjectAnimator.ofFloat(mView, "translationX", 0, 2 * mTarget));
        swipe.setDuration(300);
        swipe.addListener(this);
        swipe.start();
        // Send Friend Request
        sendFriendRequest(PhotosFragment.mFrontPhoto);
    }

    public void swipeLeft() {
        AnimatorSet swipe = new AnimatorSet();
        swipe.playTogether(ObjectAnimator.ofFloat(mView, "translationX", 0, -2 * mTarget));
        swipe.setDuration(300);
        swipe.addListener(this);
        swipe.start();
    }


    @Override
    public void onAnimationEnd(Animator animation) {
        mView.clearAnimation();
        mView.setRotation(0);
        mView.setTranslationX(0);
        mView.setTranslationY(0);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mViewWidth, mViewHeight);
        params.leftMargin = mInitialPositionX;
        params.topMargin = mInitialPositionY;
        mView.setLayoutParams(params);

        // Resume Back position
        params = (RelativeLayout.LayoutParams) mBackView.getLayoutParams();
        params.topMargin = mInitialPositionY + mTopBottomDelta;
        mBackView.setLayoutParams(params);

        changePhotos();
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }


    private void sendFriendRequest(Photo photo) {
        // check if is already a friend

        String sender = MainActivity.CURRENT_USER.getObjectId();
        String receiver = photo.getAuthor();
        WindpicApplication.FIREBASE.child("PENDING_FRIEND_REQUESTS").child(receiver).child(sender).setValue(sender);
    }

    private void changePhotos() {
        // unpin mView image, change mView image, fade in mBackView
        if (!Methods.isInternetAvailable(mContext)) {
            Toast.makeText(mContext, "No internet connection!", Toast.LENGTH_LONG).show();
            return;
        }
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

}
