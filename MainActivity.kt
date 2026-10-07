package com.killer.systemservice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import com.killer.systemservice.databinding.ActivityMainBinding
import com.killer.systemservice.engine.ScanEngine
import com.killer.systemservice.engine.ScanMode
import com.killer.systemservice.engine.ThreatInfo
import com.killer.systemservice.service.KillerAccessibilityService
import kotlinx.coroutines.*
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var selectedMode: ScanMode = ScanMode.NORMAL
    private lateinit var scanEngine: ScanEngine
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var threatsAdapter: ThreatsAdapter? = null
    
    companion object {
        private const val REQUEST_STORAGE = 1001
        private const val REQUEST_SHIZUKU = 1002
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        scanEngine = ScanEngine(this)
        setupModeSelection()
        setupScanButton()
        
        // 默认选中普通档
        selectMode(binding.cardNormal, ScanMode.NORMAL)
    }
    
    private fun setupModeSelection() {
        binding.cardNormal.setOnClickListener {
            selectMode(it as MaterialCardView, ScanMode.NORMAL)
        }
        
        binding.cardAdvanced.setOnClickListener {
            selectMode(it as MaterialCardView, ScanMode.ADVANCED)
        }
        
        binding.cardUltimate.setOnClickListener {
            selectMode(it as MaterialCardView, ScanMode.ULTIMATE)
        }
        
        binding.cardDeath.setOnClickListener {
            selectMode(it as MaterialCardView, ScanMode.DEATH)
        }
    }
    
    private fun selectMode(card: MaterialCardView, mode: ScanMode) {
        // 重置所有卡片
        listOf(binding.cardNormal, binding.cardAdvanced, 
               binding.cardUltimate, binding.cardDeath).forEach {
            it.strokeWidth = resources.getDimensionPixelSize(R.dimen.card_stroke_normal)
            it.strokeColor = ContextCompat.getColor(this, R.color.gray_medium)
        }
        
        // 高亮选中的卡片
        card.strokeWidth = resources.getDimensionPixelSize(R.dimen.card_stroke_selected)
        card.strokeColor = ContextCompat.getColor(this, R.color.red_dark)
        
        selectedMode = mode
    }
    
    private fun setupScanButton() {
        binding.btnStartScan.setOnClickListener {
            when (selectedMode) {
                ScanMode.NORMAL -> {
                    if (checkStoragePermission()) {
                        startScan()
                    }
                }
                ScanMode.ADVANCED -> {
                    if (checkStoragePermission() && checkAccessibilityEnabled()) {
                        startScan()
                    }
                }
                ScanMode.ULTIMATE -> {
                    if (checkStoragePermission() && checkAccessibilityEnabled() && checkShizuku()) {
                        startScan()
                    }
                }
                ScanMode.DEATH -> {
                    if (checkAllPermissions() && checkTermux()) {
                        startScan()
                    }
                }
            }
        }
        
        binding.btnCleanAll.setOnClickListener {
            threatsAdapter?.getThreats()?.let { threats ->
                cleanThreats(threats)
            }
        }
    }
    
    private fun checkStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                AlertDialog.Builder(this)
                    .setTitle("需要文件管理权限")
                    .setMessage("普通档需要「管理所有文件」权限来扫描应用")
                    .setPositiveButton("去设置") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setNegativeButton("取消", null)
                    .show()
                return false
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                           Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE
                )
                return false
            }
        }
        return true
    }
    
    private fun checkAccessibilityEnabled(): Boolean {
        if (!KillerAccessibilityService.isEnabled(this)) {
            AlertDialog.Builder(this)
                .setTitle("需要无障碍权限")
                .setMessage("高级档及以上需要无障碍权限来深度检测恶意应用")
                .setPositiveButton("去设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
            return false
        }
        return true
    }
    
    private fun checkShizuku(): Boolean {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(REQUEST_SHIZUKU)
                return false
            }
            
            if (!Shizuku.pingBinder()) {
                Toast.makeText(this, "Shizuku 未运行，请先启动 Shizuku", Toast.LENGTH_LONG).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Shizuku 未安装或未启动", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
    
    private fun checkAllPermissions(): Boolean {
        return checkStoragePermission() && checkAccessibilityEnabled() && checkShizuku()
    }
    
    private fun checkTermux(): Boolean {
        val termuxInstalled = try {
            packageManager.getPackageInfo("com.termux", 0)
            true
        } catch (e: Exception) {
            false
        }
        
        if (!termuxInstalled) {
            AlertDialog.Builder(this)
                .setTitle("需要 Termux")
                .setMessage("死亡档需要配合 Termux 进行系统级清理")
                .setPositiveButton("了解", null)
                .show()
            return false
        }
        return true
    }
    
    private fun startScan() {
        binding.resultCard.visibility = View.VISIBLE
        binding.tvScanStatus.text = "正在扫描..."
        binding.rvThreats.visibility = View.GONE
        binding.btnCleanAll.visibility = View.GONE
        binding.btnStartScan.isEnabled = false
        
        scope.launch {
            try {
                val threats = withContext(Dispatchers.IO) {
                    scanEngine.scan(selectedMode)
                }
                
                displayResults(threats)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "扫描出错: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnStartScan.isEnabled = true
            }
        }
    }
    
    private fun displayResults(threats: List<ThreatInfo>) {
        if (threats.isEmpty()) {
            binding.tvScanStatus.text = "✓ 未发现威胁"
            binding.tvScanStatus.setTextColor(ContextCompat.getColor(this, R.color.green_success))
        } else {
            binding.tvScanStatus.text = "⚠ 发现 ${threats.size} 个威胁"
            binding.tvScanStatus.setTextColor(ContextCompat.getColor(this, R.color.red_light))
            
            threatsAdapter = ThreatsAdapter(threats)
            binding.rvThreats.layoutManager = LinearLayoutManager(this)
            binding.rvThreats.adapter = threatsAdapter
            binding.rvThreats.visibility = View.VISIBLE
            binding.btnCleanAll.visibility = View.VISIBLE
        }
    }
    
    private fun cleanThreats(threats: List<ThreatInfo>) {
        AlertDialog.Builder(this)
            .setTitle("确认清除")
            .setMessage("即将清除 ${threats.size} 个威胁，此操作不可逆")
            .setPositiveButton("确认清除") { _, _ ->
                performClean(threats)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun performClean(threats: List<ThreatInfo>) {
        scope.launch {
            val progressDialog = AlertDialog.Builder(this@MainActivity)
                .setTitle("正在清除")
                .setMessage("请稍候...")
                .setCancelable(false)
                .create()
            progressDialog.show()
            
            try {
                val result = withContext(Dispatchers.IO) {
                    scanEngine.clean(threats, selectedMode)
                }
                
                progressDialog.dismiss()
                
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("清除完成")
                    .setMessage("成功: ${result.success}\n失败: ${result.failed}")
                    .setPositiveButton("重新扫描") { _, _ ->
                        startScan()
                    }
                    .setNegativeButton("关闭", null)
                    .show()
                    
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(this@MainActivity, "清除失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
