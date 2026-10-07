package com.killer.systemservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.killer.systemservice.engine.ThreatInfo

class ThreatsAdapter(private val threats: List<ThreatInfo>) : 
    RecyclerView.Adapter<ThreatsAdapter.ThreatViewHolder>() {
    
    class ThreatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAppName: TextView = view.findViewById(R.id.tvAppName)
        val tvPackageName: TextView = view.findViewById(R.id.tvPackageName)
        val tvReason: TextView = view.findViewById(R.id.tvReason)
        val tvThreatLevel: TextView = view.findViewById(R.id.tvThreatLevel)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_threat, parent, false)
        return ThreatViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ThreatViewHolder, position: Int) {
        val threat = threats[position]
        holder.tvAppName.text = threat.appName
        holder.tvPackageName.text = threat.packageName
        holder.tvReason.text = threat.reason
        
        val levelText = when (threat.threatLevel) {
            5 -> "极高危"
            4 -> "高危"
            3 -> "中危"
            2 -> "低危"
            else -> "可疑"
        }
        holder.tvThreatLevel.text = levelText
        
        val levelColor = when (threat.threatLevel) {
            5 -> android.graphics.Color.parseColor("#D32F2F")
            4 -> android.graphics.Color.parseColor("#F44336")
            3 -> android.graphics.Color.parseColor("#FF9800")
            2 -> android.graphics.Color.parseColor("#FFC107")
            else -> android.graphics.Color.parseColor("#9E9E9E")
        }
        holder.tvThreatLevel.setTextColor(levelColor)
    }
    
    override fun getItemCount() = threats.size
    
    fun getThreats() = threats
}
