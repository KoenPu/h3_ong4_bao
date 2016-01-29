package pu.hongbao.utils;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by koen on 2016/1/29.
 */
public class HongbaoSignature {

    private String sender, content, time;

    public boolean generateSignature(AccessibilityNodeInfo node) {
        try {

            AccessibilityNodeInfo hongbaoNode = node.getParent();
            String hongbaoContent = hongbaoNode.getChild(0).getText().toString();

            if (hongbaoContent == null) {
                return false;
            }

            AccessibilityNodeInfo messageNode = hongbaoNode.getParent();

            String[] hongbaoInfo = getDesFromNode(messageNode);

            if (this.getSignature(hongbaoInfo[0], hongbaoContent, hongbaoInfo[1])
                    .equals(this.toString())) {
                return false;
            }

            this.sender = hongbaoInfo[0];
            this.time = hongbaoInfo[1];
            this.content = hongbaoContent;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getSignature(this.sender, this.content, this.time);
    }

    private String getSignature(String... strings) {
        String signature = "";
        for (String str: strings) {
            if (str == null) {
                return null;
            }
            signature += str + "|";
        }

        return signature.substring(0, signature.length() - 1);
    }

    private String[] getDesFromNode(AccessibilityNodeInfo nodeInfo) {
        int count = nodeInfo.getChildCount();
        String[] result = {"unknownSender", "unknownTime"};
        for (int i = 0 ; i < count ; i++) {
            AccessibilityNodeInfo thisNode = nodeInfo.getChild(i);
            if ("android.widget.ImageView".equals(thisNode.getClassName())) {
                CharSequence contentDes = thisNode.getContentDescription();
                if (contentDes != null) {
                    result[0] = contentDes.toString();
                }
            } else if ("android.widget.TextView".equals(thisNode.getClassName())) {
                CharSequence thisNodeText = thisNode.getText();
                if (thisNodeText != null) {
                    result[1] = thisNodeText.toString();
                }
            }
        }
        return result;
    }
}
