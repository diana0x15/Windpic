package com.wind.windpic.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wind.windpic.tools.Methods;

import java.io.IOException;
import java.util.List;

/**
 * Created by dianapislaru on 24/08/15.
 */
public class CameraPreview {

    private static final String TAG = "CameraPreview";

    public static int currentCameraId;
    public boolean isCameraOn;

    private List<Camera.Size> mBackPreviewSizes;
    private List<Camera.Size> mBackPictureSizes;
    private List<Camera.Size> mFrontPreviewSizes;
    private List<Camera.Size> mFrontPictureSizes;

    public static int mDisplayOrientation;
    public float mDisplayRatio;
    public static int mDisplayWidth = 0;
    public static int mDisplayHeight = 0;

    public static float mBestBackPreviewRatio = 0;
    public static float mBestBackPictureRatio = 0;
    public static float mBestFrontPreviewRatio = 0;
    public static float mBestFrontPictureRatio = 0;

    public int mBestBackPreviewWidth = 0;
    public int mBestBackPreviewHeight = 0;
    public int mBestBackPictureWidth = 0;
    public int mBestBackPictureHeight = 0;
    public int mBestFrontPreviewWidth = 0;
    public int mBestFrontPreviewHeight = 0;
    public int mBestFrontPictureWidth = 0;
    public int mBestFrontPictureHeight = 0;

    private AppCompatActivity mActivity;
    private Camera mCamera;
    private SurfaceView mSurface;
    private SurfaceHolder mHolder;
    private CameraCallback mCallback;
    private RelativeLayout mContainer;
    private TakePictureCallback mPictureCallback;

    public CameraPreview(AppCompatActivity activity, SurfaceView surfaceView, RelativeLayout container, int cameraId) {
        mActivity = activity;
        mSurface = surfaceView;
        mContainer = container;
        mPictureCallback = new TakePictureCallback(mActivity);
        currentCameraId = cameraId;
        mDisplayOrientation = CameraUtils.getDisplayOrientation(mActivity, currentCameraId);

        openCamera(currentCameraId);

        if (mCamera != null) {

            mDisplayRatio = getDisplayRatio();
            if (currentCameraId == 0) {
                mBackPreviewSizes = getPreviewSizes();
                mBackPictureSizes = getPictureSizes();
                getBestPreviewSize(mBackPreviewSizes);
                getBestPictureSize(mBackPictureSizes);
            } else {
                mFrontPreviewSizes = getPreviewSizes();
                mFrontPictureSizes = getPictureSizes();
                getBestPreviewSize(mFrontPreviewSizes);
                getBestPictureSize(mFrontPictureSizes);
            }

            mHolder = surfaceView.getHolder();
            mCallback = new CameraCallback(mCamera);
            mHolder.addCallback(mCallback);
            startCameraPreview();
        } else {
            Toast.makeText(mActivity, "Camera is unavailable.", Toast.LENGTH_LONG).show();
        }
    }

    public void startCameraPreview() {
        isCameraOn = true;
        Camera.Parameters p = mCamera.getParameters();
        if (currentCameraId == 0) {
            p.setPreviewSize(mBestBackPreviewHeight, mBestBackPreviewWidth);
            p.setPictureSize(mBestBackPictureHeight, mBestBackPictureWidth);
        } else {
            p.setPreviewSize(mBestFrontPreviewHeight, mBestFrontPreviewWidth);
            p.setPictureSize(mBestFrontPictureHeight, mBestFrontPictureWidth);
        }
        mCamera.setParameters(p);
        mCamera.setDisplayOrientation(mDisplayOrientation);
    }

    public void openCamera(int cameraNumber) {
        Camera camera = null;

        try {
            camera = Camera.open(cameraNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera = camera;
    }

    public void takePicture() {
        mCamera.takePicture(null, null, mPictureCallback);
    }

    public void stopPreview() {
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pausePreview() {
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startPreview() {
        isCameraOn = true;
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchCamera(int cameraId) {
        stopPreview();
        currentCameraId = cameraId;
        mDisplayOrientation = CameraUtils.getDisplayOrientation(mActivity, currentCameraId);
        openCamera(cameraId);

        if (mCamera != null) {
            if (cameraId == 0 && mBestBackPictureHeight == 0) {
                mBackPreviewSizes = getPreviewSizes();
                mBackPictureSizes = getPictureSizes();
                getBestPreviewSize(mBackPreviewSizes);
                getBestPictureSize(mBackPictureSizes);
            } else if (cameraId == 1 && mBestFrontPreviewHeight == 0) {
                mFrontPreviewSizes = getPreviewSizes();
                mFrontPictureSizes = getPictureSizes();
                getBestPreviewSize(mFrontPreviewSizes);
                getBestPictureSize(mFrontPictureSizes);
            }
            mHolder.removeCallback(mCallback);
            mCallback = new CameraCallback(mCamera);
            setBestLayout(mContainer);
            mHolder.addCallback(mCallback);
            startCameraPreview();
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mActivity, "Camera is unavailable.", Toast.LENGTH_LONG).show();
        }
    }

    private void getBestPreviewSize(List<Camera.Size> previewSizes) {
        int bestWidth = 0;
        int bestHeight = 0;

        float minDif = 9999999;

        for (Camera.Size s : previewSizes) {
            int height = s.width;
            int width = s.height;
            float ratio = (float) height / width;
            float dif = Math.abs(mDisplayRatio - ratio);
            if ((dif < minDif) || (Methods.areEqual(dif, minDif) && height > bestHeight)) {
                minDif = dif;
                bestWidth = width;
                bestHeight = height;
            }
        }

        if (currentCameraId == 0) {
            mBestBackPreviewWidth = bestWidth;
            mBestBackPreviewHeight = bestHeight;
            mBestBackPreviewRatio = (float) bestWidth / bestHeight;
        } else {
            mBestFrontPreviewWidth = bestWidth;
            mBestFrontPreviewHeight = bestHeight;
            mBestFrontPreviewRatio = (float) bestWidth / bestHeight;
        }
    }

    private void getBestPictureSize(List<Camera.Size> pictureSizes) {
        int bestWidth = 0;
        int bestHeight = 0;
        float minDif = 9999999;

        for (Camera.Size s : pictureSizes) {
            int height = s.width;
            int width = s.height;
            float ratio = (float) height / width;
            float dif = Math.abs(mDisplayRatio - ratio);
            if ((dif < minDif) || (Methods.areEqual(dif, minDif) && height > bestHeight)) {
                minDif = dif;
                bestWidth = width;
                bestHeight = height;
            }
        }

        if (currentCameraId == 0) {
            mBestBackPictureWidth = bestWidth;
            mBestBackPictureHeight = bestHeight;
            mBestBackPictureRatio = (float) bestWidth / bestHeight;
        } else {
            mBestFrontPictureWidth = bestWidth;
            mBestFrontPictureHeight = bestHeight;
            mBestFrontPictureRatio = (float) bestWidth / bestHeight;
        }
    }

    private List<Camera.Size> getPreviewSizes() {
        Camera.Parameters parameters = null;
        if (mCamera != null) {
            parameters = mCamera.getParameters();
        }
        if (parameters != null)
            return parameters.getSupportedPreviewSizes();
        else
            return null;
    }

    private List<Camera.Size> getPictureSizes() {
        Camera.Parameters parameters = null;
        if (mCamera != null) {
            parameters = mCamera.getParameters();
        }
        if (parameters != null)
            return parameters.getSupportedPictureSizes();
        else
            return null;
    }

    private float getDisplayRatio() {

        Point displaySize = Methods.getFullDisplaySize(mActivity);
        mDisplayWidth = displaySize.x;
        mDisplayHeight = displaySize.y;

        return (float) mDisplayHeight / mDisplayWidth;
    }

    public void setBestLayout(RelativeLayout container) {
        Point dimens = getBestParameters();
        int height = dimens.y;
        int width = dimens.x;
        int margin = (width - mDisplayWidth) / 2;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);

        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.leftMargin = margin * -1;
        if (margin > 0)
            params.rightMargin = width;

        mSurface.setVisibility(View.INVISIBLE);
        container.setLayoutParams(params);
        mSurface.setVisibility(View.VISIBLE);
    }

    private Point getBestParameters() {
        Point params = new Point();
        int height = mDisplayHeight;
        int width = 0;
        if (currentCameraId == 0) {
            width = (int) (height * mBestBackPreviewRatio);
        } else {
            width = (int) (height * mBestFrontPreviewRatio);
        }
        params.x = width;
        params.y = height;
        return params;
    }

    public boolean isCameraOpened() {
        if (mCamera == null) {
            return false;
        }
        return true;
    }

}
