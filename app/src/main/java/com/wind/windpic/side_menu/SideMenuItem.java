package com.wind.windpic.side_menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wind.windpic.R;

public class SideMenuItem extends LinearLayout {

    private TextView mTitle;
    private ImageButton mIcon;
    private FrameLayout mNotificationLayout;

    public SideMenuItem(Context context) {
        super(context);
        initViews(context);
    }

    public SideMenuItem(Context context, String title, int icon) {
        super(context);
        initViews(context);
        mTitle.setText(title);
        mIcon.setImageResource(icon);
    }

    public void showNotifications() {
        if (mNotificationLayout == null) return;
        mNotificationLayout.setVisibility(View.VISIBLE);
    }

    public void hideNotifications() {
        if (mNotificationLayout == null) return;
        mNotificationLayout.setVisibility(View.INVISIBLE);
    }

    public String getTitle() {
        return mTitle.getText().toString();
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.side_menu_item, this);
        mTitle = (TextView) findViewById(R.id.side_menu_item_title);
        mNotificationLayout = (FrameLayout) findViewById(R.id.notification_button_menu_layout);
        mIcon = (ImageButton) findViewById(R.id.toolbar_button_menu);
    }
}
