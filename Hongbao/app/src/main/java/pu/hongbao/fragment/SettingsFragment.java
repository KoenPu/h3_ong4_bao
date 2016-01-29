package pu.hongbao.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.view.View;

import pu.hongbao.R;

/**
 * Created by koen on 2016/1/29.
 */
public class SettingsFragment extends PreferenceFragment{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setPrefListeners();
    }

    private void setPrefListeners() {

        Preference updatePref = findPreference("pref_etc_check_update");
        updatePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // 升级
                return false;
            }
        });

    }

    public void enterAccessibilityPage(View view) {

        Intent mAccessibleIntent = new
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(mAccessibleIntent);
    }

    public void performBack(View view) {
        getActivity().onBackPressed();
    }
}
