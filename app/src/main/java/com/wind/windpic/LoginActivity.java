package com.wind.windpic;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.wind.windpic.adapters.LoginBackgroundAdapter;
import com.wind.windpic.tools.DownloadPhoto;
import com.wind.windpic.tools.Methods;
import com.wind.windpic.tools.ProfileTracker;
import com.wind.windpic.tools.UserLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by dianapislaru on 01/11/15.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private LoginButton mLoginButton;
    private ProgressDialog mProgressDialog;
    private ViewPager mViewPager;
    public static ImageView mIndicator1;
    public static ImageView mIndicator2;
    public static ImageView mIndicator3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        Methods.setFragment(Methods.FRAGMENT_LOGIN);
        mViewPager = (ViewPager) findViewById(R.id.activity_login_viewPager);
        mIndicator1 = (ImageView) findViewById(R.id.activity_login_indicator1);
        mIndicator2 = (ImageView) findViewById(R.id.activity_login_indicator2);
        mIndicator3 = (ImageView) findViewById(R.id.activity_login_indicator3);
        mIndicator1.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_full));
        mIndicator2.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_empty));
        mIndicator3.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_empty));
        mViewPager.setAdapter(new LoginBackgroundAdapter(this));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mIndicator1.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_full));
                        mIndicator2.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_empty));
                        mIndicator3.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_empty));
                        break;
                    case 1:
                        mIndicator1.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_empty));
                        mIndicator2.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_full));
                        mIndicator3.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_empty));
                        break;
                    case 2:
                        mIndicator1.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_empty));
                        mIndicator2.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_empty));
                        mIndicator3.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_full));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (ParseUser.getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        FacebookSdk.sdkInitialize(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Logging in...");
        mProgressDialog.setCancelable(false);
        mLoginButton = (LoginButton) findViewById(R.id.login_with_facebook_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.show();
                loginWithParse();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void loginWithParse() {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, Arrays.asList("public_profile", "user_birthday"), new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException err) {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    Log.i(TAG, "exception null");
                }
                if (user != null) {
                    user.saveInBackground();
                    // Save the current Installation to Parse.
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("user", user);
                    installation.saveInBackground();
                    MainActivity.CURRENT_USER = user;
                    MainActivity.USER_LOCATION = new UserLocation(LoginActivity.this);
                    MainActivity.USER_LOCATION.connect();
                    //WindpicApplication.DATABASE = new Database(LoginActivity.this);
                    //WindpicApplication.DATABASE.onUpgrade(null, 0, 0);
                    getUserInfo();
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to login with Facebook", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void saveFacebookData() {
        Profile profile = Profile.getCurrentProfile();
        int size = Methods.getScreenSize(this).x;
        DownloadPhoto task = new DownloadPhoto(this, mProgressDialog);
        task.execute(profile.getProfilePictureUri(size, size).toString());
    }

    private int getMaxAge(int userAge) {
        if (userAge <= 14 || userAge >= 18) {
            return userAge + 3;
        } else {
            return 18;
        }
    }

    private int getMinAge(int userAge) {
        if (userAge < 18 || userAge >= 21) {
            return userAge - 3;
        } else {
            return 18;
        }
    }

    private void getUserInfo() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Application code
                        setUserInfo(object);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, gender, birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void setUserInfo(JSONObject info) {
        if (MainActivity.CURRENT_USER == null) {
            return;
        }
        String first_name = null;
        String last_name = null;
        String gender = null;
        String birthday = null;
        int distance = 0;
        String id = null;
        try {
            first_name = info.getString("first_name");
            last_name = info.getString("last_name");
            gender = info.getString("gender");
            birthday = info.getString("birthday");
            id = info.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (first_name != null) {
            MainActivity.CURRENT_USER.put("first_name", first_name);
        }
        if (last_name != null) {
            MainActivity.CURRENT_USER.put("last_name", last_name);
        }
        if (gender != null) {
            if (gender.equals("male")) {
                MainActivity.CURRENT_USER.put("gender", "M");
            }
            else {
                MainActivity.CURRENT_USER.put("gender", "F");
            }
        }
        if (distance == 0) {
            MainActivity.CURRENT_USER.put("distance", 10);
        }
        if (birthday != null) {
            int age = getAge(birthday);
            MainActivity.CURRENT_USER.put("age", age);
            if (MainActivity.CURRENT_USER.get("minAge") == null) {
                MainActivity.CURRENT_USER.put("minAge", getMinAge(age));
            }
            if (MainActivity.CURRENT_USER.get("maxAge") == null) {
                MainActivity.CURRENT_USER.put("maxAge", getMaxAge(age));
            }
            MainActivity.CURRENT_USER.put("birthday", birthday);
        }

        if (MainActivity.CURRENT_USER.get("searchGender") == null) {
            MainActivity.CURRENT_USER.put("searchGender", "FM");
        }
        if (MainActivity.CURRENT_USER.get("snapSearching") == null) {
            MainActivity.CURRENT_USER.put("snapSearching", "true");
        }

        if (MainActivity.PROFILE_TRACKER == null) {
            MainActivity.PROFILE_TRACKER = new ProfileTracker();
        }
        if (!MainActivity.PROFILE_TRACKER.isTracking()) {
            MainActivity.PROFILE_TRACKER.startTracking();
        }
        int size = Methods.getScreenSize(this).x;
        DownloadPhoto task = new DownloadPhoto(this, mProgressDialog);
        task.execute(Profile.getCurrentProfile().getProfilePictureUri(size, size).toString());
    }

    public static int getAge(String birthday) {
        if (birthday == null)
            return 18;
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Date date = null;
        try {
            date = format.parse(birthday);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        if (date == null)
            return 18;

        long mil = date.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mil);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);


        // Calculate age
        Calendar birthdate = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        birthdate.set(year, month, day);

        int age = today.get(Calendar.YEAR) - birthdate.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birthdate.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        return age;
    }

}
