package com.killer.systemservice.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.killer.systemservice.service.KillerForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                // 开机后启动前台服务
                val serviceIntent = Intent(it, KillerForegroundService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.startForegroundService(serviceIntent)
                } else {
                    it.startService(serviceIntent)
                }
            }
        }
    }
}
