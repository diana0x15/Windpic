package com.wind.windpic.adapters;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wind.windpic.R;
import com.wind.windpic.tools.Methods;

/**
 * Created by dianapislaru on 11/11/15.
 */
public class LoginBackgroundAdapter extends PagerAdapter {

    private Activity mActivity;

    public LoginBackgroundAdapter(Activity activity) {
        mActivity = activity;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.login_background_item, collection, false);
        ImageView image = (ImageView) layout.findViewById(R.id.login_background_image);
        TextView text = (TextView) layout.findViewById(R.id.login_background_text);

        switch (position) {
            case 0:
                image.setImageBitmap(Methods.decodeSampledBitmapFromResource(mActivity.getResources(),
                        R.drawable.login_image_1_small, Methods.getSmallDisplaySize(mActivity)));
                break;
            case 1:
                image.setImageBitmap(Methods.decodeSampledBitmapFromResource(mActivity.getResources(),
                        R.drawable.login_image_2_small, Methods.getSmallDisplaySize(mActivity)));
                break;
            case 2:
                image.setImageBitmap(Methods.decodeSampledBitmapFromResource(mActivity.getResources(),
                        R.drawable.login_image_3_small, Methods.getSmallDisplaySize(mActivity)));
                break;
        }
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}
