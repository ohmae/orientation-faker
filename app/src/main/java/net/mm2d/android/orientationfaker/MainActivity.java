/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import net.mm2d.android.orientationfaker.orientation.OrientationHelper;
import net.mm2d.android.orientationfaker.orientation.OrientationIdManager;
import net.mm2d.android.orientationfaker.orientation.OrientationIdManager.OrientationId;
import net.mm2d.android.orientationfaker.orientation.OverlayPermissionHelper;
import net.mm2d.android.orientationfaker.settings.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MainActivity extends AppCompatActivity {
    private static final String ACTION_UPDATE = "ACTION_UPDATE";
    private static final int REQUEST_CODE = 101;
    private Settings mSettings;
    private OrientationHelper mOrientationHelper;
    private TextView mStatusDescription;
    private CheckBox mResidentCheckBox;
    private List<Pair<Integer, View>> mButtonArray = new ArrayList<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            setStatusDescription();
            setOrientationIcon();
        }
    };

    public static void notifyUpdate(@NonNull final Context context) {
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(ACTION_UPDATE));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOrientationHelper = OrientationHelper.getInstance(this);
        mSettings = new Settings(this);
        mStatusDescription = findViewById(R.id.status_description);
        mResidentCheckBox = findViewById(R.id.resident_check_box);
        mResidentCheckBox.setClickable(false);

        findViewById(R.id.status).setOnClickListener(v -> toggleStatus());
        findViewById(R.id.resident).setOnClickListener(v -> toggleResident());
        findViewById(R.id.license).setOnClickListener(v -> {
            startActivity(new Intent(this, LicenseActivity.class));
        });
        setStatusDescription();
        setResidentCheckBox();
        setUpOrientationIcons();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mReceiver, new IntentFilter(ACTION_UPDATE));

        if (mSettings.shouldResident()) {
            MainService.start(this);
        } else if (!OverlayPermissionHelper.canDrawOverlays(this)) {
            MainService.stop(this);
        }
        checkPermission();
    }

    private void checkPermission() {
        OverlayPermissionHelper.requestOverlayPermissionIfNeed(this, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE) {
            mHandler.postDelayed(this::checkPermission, 1000);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mReceiver);
    }

    private void setUpOrientationIcons() {
        for (final OrientationId id : OrientationIdManager.getList()) {
            final View button = findViewById(id.getViewId());
            mButtonArray.add(new Pair<>(id.getOrientation(), button));
            button.setOnClickListener(v -> setOrientation(id.getOrientation()));
        }
        setOrientationIcon();
    }

    private void toggleStatus() {
        if (mOrientationHelper.isEnabled()) {
            MainService.stop(this);
            if (mSettings.shouldResident()) {
                mSettings.setResident(false);
                setResidentCheckBox();
            }
        } else {
            MainService.start(this);
        }
    }

    private void setStatusDescription() {
        final int stringRes = mOrientationHelper.isEnabled() ?
                R.string.status_running : R.string.status_waiting;
        mStatusDescription.setText(stringRes);
    }

    private void toggleResident() {
        mSettings.setResident(!mSettings.shouldResident());
        setResidentCheckBox();
        if (mSettings.shouldResident() && !mOrientationHelper.isEnabled()) {
            MainService.start(this);
        }
    }

    private void setResidentCheckBox() {
        mResidentCheckBox.setChecked(mSettings.shouldResident());
    }

    private void setOrientation(final int orientation) {
        mSettings.setOrientation(orientation);
        setOrientationIcon();
        if (mOrientationHelper.isEnabled()) {
            MainService.start(this);
        }
    }

    private void setOrientationIcon() {
        final Integer orientation = mSettings.getOrientation();
        for (final Pair<Integer, View> pair : mButtonArray) {
            if (orientation.equals(pair.first)) {
                pair.second.setBackgroundResource(R.drawable.bg_icon_selected);
            } else {
                pair.second.setBackgroundResource(R.drawable.bg_icon);
            }
        }
    }
}
