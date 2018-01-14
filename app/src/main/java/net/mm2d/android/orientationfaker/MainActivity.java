/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import net.mm2d.android.orientationfaker.settings.Settings;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MainActivity extends AppCompatActivity {
    private Settings mSettings;
    private OrientationHelper mOrientationHelper;
    private TextView mStatusDescription;
    private CheckBox mResidentCheckBox;
    private View[] mIcons = new View[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOrientationHelper = OrientationHelper.getInstance(this);
        mSettings = new Settings(this);
        if (mSettings.shouldResident()) {
            mOrientationHelper.setOrientation(mSettings.getOrientation());
        }
        mStatusDescription = findViewById(R.id.status_description);
        mResidentCheckBox = findViewById(R.id.resident_check_box);
        mResidentCheckBox.setClickable(false);

        findViewById(R.id.status).setOnClickListener(v -> toggleOrientation());
        findViewById(R.id.resident).setOnClickListener(v -> toggleResident());
        setStatusDescription();
        setResidentCheckBox();
        setUpOrientationIcons();
    }

    private void setUpOrientationIcons() {
        mIcons[0] = findViewById(R.id.button_unspecified);
        mIcons[0].setOnClickListener(v -> {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        });
        mIcons[1] = findViewById(R.id.button_portrait);
        mIcons[1].setOnClickListener(v -> {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });
        mIcons[2] = findViewById(R.id.button_landscape);
        mIcons[2].setOnClickListener(v -> {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        mIcons[3] = findViewById(R.id.button_reverse_portrait);
        mIcons[3].setOnClickListener(v -> {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        });
        mIcons[4] = findViewById(R.id.button_reverse_landscape);
        mIcons[4].setOnClickListener(v -> {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        });
        setOrientationIcon();
    }

    private void toggleOrientation() {
        if (mOrientationHelper.isEnabled()) {
            mOrientationHelper.cancel();
        } else {
            mOrientationHelper.setOrientation(mSettings.getOrientation());
        }
        setStatusDescription();
    }

    private void setStatusDescription() {
        final int stringRes = mOrientationHelper.isEnabled() ?
                R.string.status_running : R.string.status_waiting;
        mStatusDescription.setText(stringRes);
    }

    private void toggleResident() {
        mSettings.setResident(!mSettings.shouldResident());
        setResidentCheckBox();
    }

    private void setResidentCheckBox() {
        mResidentCheckBox.setChecked(mSettings.shouldResident());
    }

    private void setOrientation(final int orientation) {
        mSettings.setOrientation(orientation);
        setOrientationIcon();
        if (mOrientationHelper.isEnabled()) {
            mOrientationHelper.setOrientation(orientation);
        }
    }

    private void setOrientationIcon() {
        for(final View icon : mIcons) {
            icon.setBackgroundResource(R.drawable.bg_icon);
        }
        getActiveIcon().setBackgroundResource(R.drawable.bg_icon_selected);
    }

    private View getActiveIcon() {
        switch (mSettings.getOrientation()) {
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                return mIcons[0];
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return mIcons[1];
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return mIcons[2];
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                return mIcons[3];
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                return mIcons[4];
        }
        return mIcons[0];
    }
}
