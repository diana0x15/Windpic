package com.wind.windpic.tools;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.facebook.login.LoginManager;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wind.windpic.LoginActivity;
import com.wind.windpic.MainActivity;
import com.wind.windpic.WindpicApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by dianapislaru on 09/10/15.
 */
public class DownloadProfilePhoto extends AsyncTask<String, Void, byte[]> {

    private static final String TAG = "DownloadPhoto";

    private AppCompatActivity mActivity;
    private ImageView mImageView;

    public DownloadProfilePhoto(AppCompatActivity activity, ImageView imageView) {
        mActivity = activity;
        mImageView = imageView;
    }

    // Decode image in background.
    @Override
    protected byte[] doInBackground(String... params) {
        return downloadProfilePicture(params[0]);
    }

    // Once complete, hide the ProgressDialog and open CameraFragment
    @Override
    protected void onPostExecute(byte[] data) {
        if (data != null) {
            ParseFile file = new ParseFile(data);
            MainActivity.CURRENT_USER.put("avatar", file);
            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
            MainActivity.CURRENT_USER.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    String id = MainActivity.CURRENT_USER.getObjectId();
                    List<ParseUser> friends = MainActivity.CURRENT_USER.getList("friends");
                    for (ParseUser f : friends) {
                        WindpicApplication.FIREBASE.child("UPDATE").child(f.getObjectId()).child(id).setValue(id);
                    }
                }
            });
        }
    }

    private byte[] downloadProfilePicture(String pictureUrl) {
        URL downloadURL;
        HttpURLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        try {
            downloadURL = new URL(pictureUrl);
            connection = (HttpURLConnection) downloadURL.openConnection();
            is = connection.getInputStream();

            int lenght = -1;
            byte[] buffer = new byte[1024];
            while ((lenght = is.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, lenght);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return byteBuffer.toByteArray();
    }

    private void logout() {
        MainActivity.CURRENT_USER = null;
        ParseUser.logOut();
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(mActivity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
    }

}
