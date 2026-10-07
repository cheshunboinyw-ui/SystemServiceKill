package com.killer.systemservice.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationMonitorService : NotificationListenerService() {
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // 监听病毒应用发出的通知
        sbn?.let {
            // 可以记录或拦截可疑通知
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 通知被移除时的处理
    }
}
