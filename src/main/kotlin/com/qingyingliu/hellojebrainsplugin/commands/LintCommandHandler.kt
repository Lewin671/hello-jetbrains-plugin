package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project
import com.qingyingliu.hellojebrainsplugin.services.LintService

/**
 * Lint命令处理器
 */
class LintCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        return message.contains("lint") || 
               message.lowercase().contains("lint") || 
               message.contains("检查") || 
               message.contains("问题")
    }
    
    override fun handle(message: String, project: Project): String {
        val lintService = LintService(project)
        
        // 检查是否包含路径信息
        val pathMatch = Regex("lint\\s+(.+)|检查\\s+(.+)|问题\\s+(.+)").find(message)
        if (pathMatch != null) {
            val path = pathMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
            if (path != null) {
                return lintService.getLintInfoForPath(path.trim())
            }
        }
        
        // 如果没有匹配到路径，尝试更宽松的匹配
        val looseMatch = Regex("lint\\s*(.+)|检查\\s*(.+)|问题\\s*(.+)").find(message)
        if (looseMatch != null) {
            val path = looseMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
            if (path != null) {
                return lintService.getLintInfoForPath(path.trim())
            }
        }
        
        return lintService.getLintInfo()
    }
    
    override fun getPriority(): Int = 8
} 