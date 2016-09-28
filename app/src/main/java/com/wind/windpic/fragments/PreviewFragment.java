package com.wind.windpic.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.wind.windpic.R;
import com.wind.windpic.camera.CameraPreview;
import com.wind.windpic.camera.CameraUtils;
import com.wind.windpic.tools.Methods;
import com.wind.windpic.tools.SendToUsersNearby;
import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dianapislaru on 27/08/15.
 */
public class PreviewFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "PreviewFragment";
    private static final int MEDIA_TYPE_IMAGE = 0;
    private static final String ARGUMENT_PICTURE_DATA = "picture data";
    private static final String EXTRA_FROM_MENU = "menu";

    private byte[] mPictureData;
    private Bitmap mPictureBitmap;
    private File mPictureFile;

    private ImageView mPictureImageView;
    private ImageButton mSaveButton;
    private ImageButton mCancelButton;
    private ImageButton mSendButton;
    private ImageButton mPreferencesButton;
    private ProgressDialog mProgressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        Methods.setFragment(Methods.FRAGMENT_PREVIEW);

        mPictureData = getArguments().getByteArray(ARGUMENT_PICTURE_DATA);
        mPictureBitmap = createPictureBitmap(mPictureData);
        mPictureData = rotatePicture(mPictureBitmap);

        mPictureImageView = (ImageView) view.findViewById(R.id.fragment_preview_imageView);
        mSaveButton = (ImageButton) view.findViewById(R.id.fragment_preview_save_button);
        mCancelButton = (ImageButton) view.findViewById(R.id.fragment_preview_cancel_button);
        mSendButton = (ImageButton) view.findViewById(R.id.fragment_preview_send_button);
        mPreferencesButton = (ImageButton) view.findViewById(R.id.fragment_preview_preferences_button);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Sending...");

        mPictureImageView.setImageBitmap(mPictureBitmap);
        mSaveButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
        mPreferencesButton.setOnClickListener(this);

        return view;
    }

    private void savePictureFile() {
        mPictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (mPictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions.");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFile);
            fos.write(mPictureData);
            fos.close();
            scanMedia(mPictureFile);
            Toast.makeText(getActivity(), "Photo saved.", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private Bitmap createPictureBitmap(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Matrix matrix = new Matrix();
        // Perform matrix rotations/mirrors depending on camera that took the photo
        if (CameraPreview.currentCameraId == 1) {
            float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
            Matrix matrixMirrorY = new Matrix();
            matrixMirrorY.setValues(mirrorY);

            matrix.postConcat(matrixMirrorY);
        }
        matrix.postRotate(CameraUtils.getDisplayOrientation(getActivity(), CameraPreview.currentCameraId));
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }


    public byte[] rotatePicture(Bitmap pictureBitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        pictureBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public void scanMedia(File picturePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(picturePath);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "WindPic");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onClick(View view) {
        if (view == mSaveButton) {
            savePictureFile();
            mSaveButton.setVisibility(View.GONE);
        } else if (view == mCancelButton) {
            openFragment(new CameraFragment());
        } else if (view == mSendButton) {
            if (Methods.isInternetAvailable(this.getContext())) {
                SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                if (!preferences.getBoolean("preferences", false)) {
                    openPreferencesFragment();
                } else {
                    openFragment(new CameraFragment());
                    ParseFile file = new ParseFile(mPictureData);
                    SendToUsersNearby sendPicture = new SendToUsersNearby(getActivity(), file, mProgressDialog);
                    sendPicture.send();
                }
            } else {
                Toast.makeText(this.getContext(), "No Internet connection.", Toast.LENGTH_LONG).show();
            }
        } else if (view == mPreferencesButton) {
            openPreferencesFragment();
        }
    }



    private void openPreferencesFragment() {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_FROM_MENU, false);
        fragment.setArguments(bundle);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_preview_container, fragment);
        transaction.addToBackStack("");
        transaction.commit();
    }

    private void openFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

}
