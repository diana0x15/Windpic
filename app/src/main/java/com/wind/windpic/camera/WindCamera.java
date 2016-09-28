package com.wind.windpic.camera;

import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

/**
 * Created by dianapislaru on 24/08/15.
 */
public class WindCamera {

    private static final String TAG = "WindCamera";

    public int currentCameraId;

    private AppCompatActivity mActivity;
    private CameraPreview mPreview;
    private RelativeLayout mContainer;

    public WindCamera(AppCompatActivity activity, RelativeLayout container, SurfaceView cameraView, int savedCameraId) {
        mActivity = activity;
        mContainer = container;
        currentCameraId = savedCameraId;

        mPreview = new CameraPreview(mActivity, cameraView, container, currentCameraId);

        mPreview.setBestLayout(mContainer);
    }

    public void stopPreview() {
        mPreview.stopPreview();
    }

    public void pausePreview() {
        mPreview.pausePreview();
    }


    public void takePicture() {
        mPreview.takePicture();
    }

    public boolean isCameraOn() {
        return mPreview.isCameraOpened();
    }

    public void switchCamera() {
        if (currentCameraId == 0) {
            currentCameraId = 1;

        } else {
            currentCameraId = 0;
        }
        mPreview.switchCamera(currentCameraId);
    }

}
