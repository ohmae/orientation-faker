/**
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 */

package net.mm2d.android.orientationfaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity
implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {
    private Preference status;
    private Preference start;
    private ListPreference mode;
    private Handler handler;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.main_activity);
        handler = new Handler();
        status = this.findPreference("status");
        start = this.findPreference("start");
        mode = (ListPreference) this.findPreference("mode");
        start.setOnPreferenceClickListener(this);
        this.getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus();
        setSummary();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getPreferenceScreen().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("mode".equals(key)) {
            setSummary();
            updateSetting();
        }
    }

    private void setSummary() {
        final String temp = mode.getValue();
        int index = 0;
        if (temp != null) {
            index = mode.findIndexOfValue(temp);
        }
        final String[] array = this.getResources().getStringArray(R.array.entries);
        mode.setSummary(array[index]);
    }

    private void setStatus() {
        if (MainService.isRunning()) {
            status.setSummary(R.string.status_running);
            start.setTitle(R.string.stop);
        } else {
            status.setSummary(R.string.status_waiting);
            start.setTitle(R.string.start);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (MainService.isRunning()) {
            this.stopService(new Intent(this, MainService.class));
        } else {
            this.startService(new Intent(this, MainService.class));
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                setStatus();
            }
        });
        return false;
    }

    private void updateSetting() {
        if (MainService.isRunning()) {
            this.startService(new Intent(this, MainService.class));
        }
    }
}
