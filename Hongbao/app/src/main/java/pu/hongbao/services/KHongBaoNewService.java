package pu.hongbao.services;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import pu.hongbao.utils.HongbaoSignature;

/**
 * Created by koen on 2016/1/31.
 */
public class KHongBaoNewService extends AccessibilityService{
    /**
     * 微信的包名
     */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /**
     * 拆红包类
     */
    static final String WECHAT_RECEIVER_CALSS = "ui.LuckyMoneyReceiveUI";
    /**
     * 红包详情类
     */
    static final String WECHAT_DETAIL = "ui.LuckyMoneyDetailUI";
    /**
     * 微信主界面或者是聊天界面
     */
    static final String WECHAT_LAUNCHER = "ui.LauncherUI";

    private String currentAcitivityName = WECHAT_LAUNCHER;

    private static final String WE_NOTIFICATION_TIP1 = "[微信红包]";
    private static final String WE_NOTIFICATION_TIP2 = "个联系人发来";
    private static final String WE_GET_MONEY_CH = "领取红包";
    private static final String WECHAT_VIEW_SELF_CH = "查看红包";
    private static final String WE_CHAI_HONG_BAO  = "发了一个红包";

    private static final String WECHAT_DETAILS_EN = "Details";
    private static final String WECHAT_DETAILS_CH = "红包详情";
    private static final String WECHAT_BETTER_LUCK_EN = "Better luck next time!";
    private static final String WECHAT_BETTER_LUCK_CH = "手慢了";
    private static final String WECHAT_EXPIRES_CH = "红包已失效";

    private HongbaoSignature signature = new HongbaoSignature();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {

            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: // 通知栏事件
                checkNotification(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:  //可能是进入应用时调用
                currentAcitivityName = setCurrentActivityName(event);
                if (currentAcitivityName.contains(WECHAT_LAUNCHER)) {
                    watchList(event);
                    getPacket(); // 点击红包
                } else if (currentAcitivityName.contains(WECHAT_RECEIVER_CALSS)) {
                    openPacket(); // 领取红包
                } else if (currentAcitivityName.contains(WECHAT_DETAIL)) {
                    backPacket();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:  //应用内变化
                if (currentAcitivityName.contains(WECHAT_LAUNCHER))
                    getPacket();
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void checkNotification(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String content = text.toString();
                if (content.contains(WE_NOTIFICATION_TIP1) ||
                        content.contains(WE_NOTIFICATION_TIP2)) {
                    // 打开通知
                    if (event.getParcelableData() != null &&
                            event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        try {
                            notification.contentIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private boolean watchList(AccessibilityEvent event) {
        // Not a message
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.getSource() == null)
            return false;

        List<AccessibilityNodeInfo> nodes = event.getSource().findAccessibilityNodeInfosByText(WE_NOTIFICATION_TIP1);
        if (!nodes.isEmpty()) {
            AccessibilityNodeInfo nodeToClick = nodes.get(0);  // 打开第一个list;
            CharSequence contentDescription = nodeToClick.getContentDescription();
            if (contentDescription != null && !signature.
                    getContentDescription().equals(contentDescription)) {  // 防止重复点击一个list;
                nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                signature.setContentDescription(contentDescription.toString());
                return true;
            }
        }
        return false;
    }

    private String setCurrentActivityName(AccessibilityEvent event) {
        try {
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
            );

            getPackageManager().getActivityInfo(componentName, 0);
            return componentName.flattenToShortString();
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    // 领取红包
    private void getPacket() {
        //AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        /*if (rootNode != null) {
            List<AccessibilityNodeInfo> nodeInfos = rootNode
                    .findAccessibilityNodeInfosByText(WE_CHAI_HONG_BAO);
        }*/
    }

    // 打开红包
    private void openPacket() {

    }

    private void backPacket() {

    }

}
