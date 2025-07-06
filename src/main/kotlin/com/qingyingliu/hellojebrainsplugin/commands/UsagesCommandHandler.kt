package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project
import com.qingyingliu.hellojebrainsplugin.services.UsagesService

/**
 * Usages命令处理器
 */
class UsagesCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        return message.contains("usages") || message.contains("引用") || message.contains("用法")
    }
    
    override fun handle(message: String, project: Project): String {
        val usagesService = UsagesService(project)
        
        // 检查是否包含类名或方法名参数
        val nameMatch = Regex("usages\\s+(.+)|引用\\s+(.+)|用法\\s+(.+)").find(message)
        if (nameMatch != null) {
            val name = nameMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
            if (name != null) {
                return usagesService.getUsagesForName(name.trim())
            }
        }
        
        // 如果没有匹配到名称，尝试更宽松的匹配
        val looseMatch = Regex("usages\\s*(.+)|引用\\s*(.+)|用法\\s*(.+)").find(message)
        if (looseMatch != null) {
            val name = looseMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
            if (name != null) {
                return usagesService.getUsagesForName(name.trim())
            }
        }
        
        return usagesService.getUsagesHelp()
    }
    
    override fun getPriority(): Int = 9
} 