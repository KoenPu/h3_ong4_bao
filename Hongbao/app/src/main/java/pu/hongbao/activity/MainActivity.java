package pu.hongbao.activity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.List;

import pu.hongbao.R;
import pu.hongbao.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final Intent mAccessibleIntent =
            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

    private Button switchPlugin;
    private ImageButton btnSetting;

    private final static String APPKEY = "56ac106e67e58e875f000ba3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        AnalyticsConfig.setAppkey(this, APPKEY);
        UmengUpdateAgent.setAppkey(APPKEY);
        UmengUpdateAgent.update(this);
        initView();
        updateServiceStatus();
        setPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initView() {
        switchPlugin = (Button) findViewById(R.id.button_accessible);
        switchPlugin.setOnClickListener(this);
        btnSetting = (ImageButton) findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(this);
    }

    private void updateServiceStatus() {
        boolean serviceEnabled = false;

        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> accessibilityServiceInfos =
                accessibilityManager.getEnabledAccessibilityServiceList
                        (AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServiceInfos) {
            if (info.getId().equals(getPackageName() + "/.PKHongbaoService")) {
                serviceEnabled = true;
                break;
            }
        }

        if (serviceEnabled) {
            switchPlugin.setText(R.string.service_off);
        } else {
            switchPlugin.setText(R.string.service_on);
        }

    }

    private void setPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    /*private void openSetting() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.replace(R.id.setting_layout, settingsFragment);
        fragmentTransaction.commit();
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_accessible:
                startActivity(mAccessibleIntent);
                MobclickAgent.onEvent(this, "setting");
                break;

            case R.id.btn_setting:
                //openSetting();
                break;
        }
    }
}
