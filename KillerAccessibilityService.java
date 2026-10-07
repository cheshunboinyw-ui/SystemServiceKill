package com.antivirus.systemservicekiller;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Handler;
import android.os.Looper;
import java.util.List;

public class KillerAccessibilityService extends AccessibilityService {
    private Handler handler = new Handler(Looper.getMainLooper());
    private String targetPackage;
    private String action;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            action = intent.getStringExtra("action");
            targetPackage = intent.getStringExtra("package");
            
            if ("force_stop".equals(action)) {
                performForceStop(targetPackage);
            } else if ("clear_data".equals(action)) {
                performClearData(targetPackage);
            }
        }
        return START_NOT_STICKY;
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        
        // 自动点击确认按钮
        clickButtonByText(rootNode, "确定");
        clickButtonByText(rootNode, "强行停止");
        clickButtonByText(rootNode, "卸载");
        clickButtonByText(rootNode, "清除数据");
        
        rootNode.recycle();
    }
    
    @Override
    public void onInterrupt() {}
    
    private void performForceStop(String packageName) {
        handler.postDelayed(() -> {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + packageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                
                handler.postDelayed(() -> {
                    AccessibilityNodeInfo root = getRootInActiveWindow();
                    if (root != null) {
                        clickButtonByText(root, "强行停止");
                        clickButtonByText(root, "强制停止");
                        clickButtonByText(root, "确定");
                        root.recycle();
                    }
                }, 1000);
            } catch (Exception e) {}
        }, 500);
    }
    
    private void performClearData(String packageName) {
        handler.postDelayed(() -> {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + packageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                
                handler.postDelayed(() -> {
                    AccessibilityNodeInfo root = getRootInActiveWindow();
                    if (root != null) {
                        clickButtonByText(root, "存储");
                        handler.postDelayed(() -> {
                            AccessibilityNodeInfo root2 = getRootInActiveWindow();
                            if (root2 != null) {
                                clickButtonByText(root2, "清除数据");
                                handler.postDelayed(() -> {
                                    AccessibilityNodeInfo root3 = getRootInActiveWindow();
                                    if (root3 != null) {
                                        clickButtonByText(root3, "确定");
                                        root3.recycle();
                                    }
                                }, 500);
                                root2.recycle();
                            }
                        }, 500);
                        root.recycle();
                    }
                }, 1000);
            } catch (Exception e) {}
        }, 500);
    }
    
    private void clickButtonByText(AccessibilityNodeInfo root, String text) {
        if (root == null) return;
        
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo node : nodes) {
            if (node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            AccessibilityNodeInfo parent = node.getParent();
            if (parent != null && parent.isClickable()) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                parent.recycle();
                break;
            }
        }
    }
}
