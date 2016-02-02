package pu.hongbao.services;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import pu.hongbao.utils.HongbaoSignature;

/**
 * Created by koen on 2016/1/31.
 */
public class KHongBaoNewService extends AccessibilityService {
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
    private static final String WE_VIEW_SELF_CH = "查看红包";
    private static final String WE_CHAI_HONG_BAO = "发了一个红包";

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
                watchNotification(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:  //可能是进入应用时调用
                currentAcitivityName = setCurrentActivityName(event);
                // 用于获得窗口的名字。
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:  //应用内变化
                // 用于实际操作。
                if (currentAcitivityName.contains(WECHAT_LAUNCHER)) {
                    if (watchList(event)) break; //在聊天列表
                    watchChat(); //在聊天页面
                } else if (currentAcitivityName.contains(WECHAT_RECEIVER_CALSS)) {
                    openPacket(); // 领取红包
                } else if (currentAcitivityName.contains(WECHAT_DETAIL)) {
                    backPacket();
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void watchNotification(AccessibilityEvent event) {
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

    //监控聊天列表，并在发现红包时打开。
    private boolean watchList(AccessibilityEvent event) {
        if (event.getSource() != null) {
            List<AccessibilityNodeInfo> nodes = event.getSource().findAccessibilityNodeInfosByText(WE_NOTIFICATION_TIP1);
            if (!nodes.isEmpty()) {
                Log.e("koen","nodes的数量为："+ nodes.size());
                AccessibilityNodeInfo nodeToClick = nodes.get(0);  // 打开第一个list;
                CharSequence contentDescription = nodeToClick.getContentDescription();
                if (contentDescription != null && !signature.getContentDescription().equals(contentDescription)) {
                    nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    signature.setContentDescription(contentDescription.toString());
                    return true;
                }
            }
        }
        return false;
    }

    // 监控红包，并在发现时打开。
    private void watchChat() {
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo != null) {
            String excludeWords = "";
            AccessibilityNodeInfo node = getLastNode(rootNodeInfo, WE_GET_MONEY_CH, WE_VIEW_SELF_CH);
            if (node != null && signature.generateSignature(node, excludeWords)) {
                node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

    }

    private AccessibilityNodeInfo getLastNode(AccessibilityNodeInfo nodeInfo, String... texts ) {
        int bottom = 0;
        Rect bounds = new Rect();
        AccessibilityNodeInfo lastNode = null;
        if (texts != null) {
            for (String text : texts) {

                List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);
                if (nodes != null) {
                    AccessibilityNodeInfo node = nodes.get(nodes.size() - 1);
                    //通过边界来确定最后一个node。
                    node.getBoundsInScreen(bounds);
                    if (bounds.bottom > bottom) {

                        Log.e("koen", "bounds.bottom==" + bounds.bottom);
                        bottom = bounds.bottom;
                        lastNode = node;
                    }

                }
            }
            return lastNode;
        } else return null;
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



    // 打开红包
    private void openPacket() {
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        AccessibilityNodeInfo node = (rootNodeInfo.getChildCount() > 3) ? rootNodeInfo.getChild(3) : null;
        Log.e("koen", "rootNodeInfo==="+ rootNodeInfo.getChildCount());
        if (node != null && "android.widget.Button".equals(node.getClassName())) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return;
        }

    }

    private void backPacket() {
        /* 戳开红包，红包已被抢完，遍历节点匹配“红包详情”和“手慢了” */
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        boolean hasNodes = this.hasOneOfThoseNodes(rootNodeInfo,
                WECHAT_BETTER_LUCK_CH, WECHAT_DETAILS_CH,
                WECHAT_BETTER_LUCK_EN, WECHAT_DETAILS_EN, WECHAT_EXPIRES_CH);
        if (hasNodes) {
            performGlobalAction(GLOBAL_ACTION_BACK);
        }

    }

    private boolean hasOneOfThoseNodes(AccessibilityNodeInfo node, String... texts) {
        for (String text : texts) {
            if (text == null) continue;

            List<AccessibilityNodeInfo> nodes = node.findAccessibilityNodeInfosByText(text);

            if (!nodes.isEmpty()) return true;
        }
        return false;
    }

}
