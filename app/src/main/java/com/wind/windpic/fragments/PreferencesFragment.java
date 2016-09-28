package com.wind.windpic.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.tools.Methods;
import com.wind.windpic.views.RangeSeekBar;

/**
 * Created by dianapislaru on 14/10/15.
 */
public class PreferencesFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PreferencesFragment";
    private static final String EXTRA_FROM_MENU = "menu";
    private static final String PREFERENCES = "preferences";

    private Button mMenuSaveButton;
    private Button mPhotosSaveButton;
    private Button mCancelButton;
    private CheckBox mMCheckBox;
    private CheckBox mFCheckBox;
    private RangeSeekBar<Integer> mDistanceSeekBar;
    private RangeSeekBar<Integer> mAgeSeekBar;
    private LinearLayout mMenuButtonsLayout;
    private LinearLayout mPhotosButtonsLayout;
    private CheckBox mSnapSearchingCheckBox;

    // Toolbar
    private Toolbar mToolbar;
    private TextView mTitle;
    private TextView mTitleStatus;
    private ImageButton mMenuButton;
    private ImageButton mCameraButton;
    private ImageButton mPhotosButton;

    private Activity mActivity;
    private int mMinAge;
    private int mMaxAge;
    private int mUserAge;
    private boolean mM;
    private boolean mF;
    private int mDistance;
    private boolean mSearching;
    private boolean mFromMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Methods.setFragment(Methods.FRAGMENT_PREFERENCES);
        mActivity = getActivity();
        if (getArguments() != null) {
            mFromMenu = getArguments().getBoolean(EXTRA_FROM_MENU);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Methods.disableFullScreen(mActivity);
        View view = inflater.inflate(R.layout.fragment_preferences, container, false);
        setUpToolbar(view);
        setHasOptionsMenu(true);

        initViews(view);
        setValues();
        setListeners();

        return view;
    }

    private void initViews(View view) {
        mPhotosSaveButton = (Button) view.findViewById(R.id.fragment_preferences_photos_save_button);
        mMenuSaveButton = (Button) view.findViewById(R.id.fragment_preferences_menu_save_button);
        mCancelButton = (Button) view.findViewById(R.id.fragment_preferences_cancel_button);
        mMCheckBox = (CheckBox) view.findViewById(R.id.fragment_preferences_M_checkBox);
        mFCheckBox = (CheckBox) view.findViewById(R.id.fragment_preferences_F_checkBox);
        mAgeSeekBar = (RangeSeekBar) view.findViewById(R.id.fragment_preferences_age_seekBar);
        mDistanceSeekBar = (RangeSeekBar) view.findViewById(R.id.fragment_preferences_distance_seekBar);
        mPhotosButtonsLayout = (LinearLayout) view.findViewById(R.id.fragment_preferences_buttons_layout);
        mMenuButtonsLayout = (LinearLayout) view.findViewById(R.id.fragment_preferences_menu_buttons_layout);
        mSnapSearchingCheckBox = (CheckBox) view.findViewById(R.id.fragment_preferences_snap_searching_checkbox);

        mPhotosSaveButton.setOnClickListener(this);
        mMenuSaveButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        if (mFromMenu) {
            mMenuButtonsLayout.setVisibility(View.VISIBLE);
            mPhotosButtonsLayout.setVisibility(View.INVISIBLE);
        } else {
            mMenuButtonsLayout.setVisibility(View.INVISIBLE);
            mPhotosButtonsLayout.setVisibility(View.VISIBLE);
        }

    }

    private void setValues() {
        // Get preferences from Parse Current User
        String searchGender = MainActivity.CURRENT_USER.getString("searchGender");
        if (searchGender.equals("FM")) {
            mM = true;
            mF = true;
        } else if (searchGender.equals("F")) {
            mF = true;
        } else {
            mM = true;
        }
        mUserAge = MainActivity.CURRENT_USER.getInt("age");
        mMaxAge = MainActivity.CURRENT_USER.getInt("maxAge");
        mMinAge = MainActivity.CURRENT_USER.getInt("minAge");
        mDistance = MainActivity.CURRENT_USER.getInt("distance");
        String searching = MainActivity.CURRENT_USER.getString("snapSearching");
        mSearching = searching.equals("true");
        mMCheckBox.setChecked(mM);
        mFCheckBox.setChecked(mF);
        if (mUserAge >= 18) {
            mAgeSeekBar.setRangeValues(18, 60);
        } else {
            mAgeSeekBar.setRangeValues(12, 17);
        }
        mAgeSeekBar.setSelectedMaxValue(mMaxAge);
        mAgeSeekBar.setSelectedMinValue(mMinAge);
        mDistanceSeekBar.setRangeValues(1, 60);
        mDistanceSeekBar.setSelectedMaxValue(mDistance);
        mSnapSearchingCheckBox.setChecked(mSearching);
        if (mSearching) {
            mSnapSearchingCheckBox.setText("ON");
        } else {
            mSnapSearchingCheckBox.setText("OFF");
        }
    }

    private void setListeners() {

        mMCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mM = b;
                if (!mM && !mF) {
                    mMenuSaveButton.setEnabled(false);
                    mPhotosSaveButton.setEnabled(false);
                } else {
                    mMenuSaveButton.setEnabled(true);
                    mPhotosSaveButton.setEnabled(true);
                }
            }
        });

        mFCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mF = b;
                if (!mM && !mF) {
                    mMenuSaveButton.setEnabled(false);
                    mPhotosSaveButton.setEnabled(false);
                } else {
                    mMenuSaveButton.setEnabled(true);
                    mPhotosSaveButton.setEnabled(true);
                }
            }
        });

        mAgeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                mMaxAge = maxValue;
                mMinAge = minValue;
            }
        });

        mDistanceSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                mDistance = maxValue;
            }
        });

        mSnapSearchingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mSearching = b;
                if (MainActivity.CURRENT_USER == null) {
                    return;
                }
                if (mSearching) {
                    MainActivity.CURRENT_USER.put("snapSearching", "true");
                    mSnapSearchingCheckBox.setText("ON");
                } else {
                    MainActivity.CURRENT_USER.put("snapSearching", "false");
                    mSnapSearchingCheckBox.setText("OFF");
                }
            }
        }
    );
}

    private void savePreferences() {
        if (MainActivity.CURRENT_USER != null) {
            MainActivity.CURRENT_USER.put("minAge", mMinAge);
            MainActivity.CURRENT_USER.put("maxAge", mMaxAge);
            MainActivity.CURRENT_USER.put("distance", mDistance);
            if (mM && mF) {
                MainActivity.CURRENT_USER.put("searchGender", "FM");
            } else if (mM) {
                MainActivity.CURRENT_USER.put("searchGender", "M");
            } else if (mF) {
                MainActivity.CURRENT_USER.put("searchGender", "F");
            }
            if (mSearching) {
                MainActivity.CURRENT_USER.put("snapSearching", "true");
            } else {
                MainActivity.CURRENT_USER.put("snapSearching", "false");
            }
            MainActivity.CURRENT_USER.saveEventually();
        }
        Toast.makeText(mActivity, "Preferences saved", Toast.LENGTH_SHORT).show();
    }

    private void setUpToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.fragment_preferences_toolbar);
        mTitle = (TextView) view.findViewById(R.id.toolbar_title);
        mTitleStatus = (TextView) view.findViewById(R.id.toolbar_status);
        mTitle.setText("Settings");
        mMenuButton = (ImageButton) view.findViewById(R.id.toolbar_button_menu);
        mCameraButton = (ImageButton) view.findViewById(R.id.toolbar_button_left);
        mPhotosButton = (ImageButton) view.findViewById(R.id.toolbar_button_right);
        mCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_camera_white_24dp));
        if (mFromMenu) {
            mPhotosButton.setImageBitmap(null);
            mMenuButton.setOnClickListener(this);
            mCameraButton.setOnClickListener(this);
        } else {
            mMenuButton.setImageBitmap(null);
            mCameraButton.setImageBitmap(null);
            mPhotosButton.setImageBitmap(null);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                }
            });
        }
        mToolbar.setTitle("");
        mTitleStatus.setTextSize(0);
    }

    private void openFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        if (view == mMenuSaveButton) {
            SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(PREFERENCES, true);
            editor.commit();
            savePreferences();
        } else if (view == mPhotosSaveButton) {
            SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(PREFERENCES, true);
            editor.commit();
            savePreferences();
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        } else if (view == mCancelButton) {
            SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(PREFERENCES, true);
            editor.commit();
            if (!mFromMenu) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        } else if (view == mMenuButton) {
            MainActivity.SIDE_MENU.open();
        } else if (view == mCameraButton) {
            openFragment(new CameraFragment());
        }
    }
}
