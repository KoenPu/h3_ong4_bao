package pu.hongbao.activity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.List;

import pu.hongbao.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final Intent mAccessibleIntent =
            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

    private Button switchPlugin;
    private ImageView btnSetting;
    private AnimationDrawable animationDrawable;
    private TextView textView, version;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        UmengUpdateAgent.update(this);
        initView();
        updateServiceStatus();
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
        btnSetting = (ImageView) findViewById(R.id.btn_setting);
        animationDrawable = (AnimationDrawable) btnSetting.getBackground();
        animationDrawable.start();

        findViewById(R.id.statement).setOnClickListener(this);
        findViewById(R.id.check_update).setOnClickListener(this);
        findViewById(R.id.use_teaching).setOnClickListener(this);
        textView = (TextView) findViewById(R.id.about_layout_textview);
        version = (TextView) findViewById(R.id.textview_version);
        version.setText(getVersion());
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_accessible:
                startActivity(mAccessibleIntent);
                MobclickAgent.onEvent(this, "setting");
                break;

            case R.id.check_update:
                UmengUpdateAgent.forceUpdate(this);
                break;

            case R.id.statement:
                textView.setText(R.string.disclaimer);
                break;

            case R.id.use_teaching:
                textView.setText(R.string.teaching);
        }
    }

    /**
     * 2  * 获取版本号
     * 3  * @return 当前应用的版本号
     * 4
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return this.getString(R.string.version_name) + version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
