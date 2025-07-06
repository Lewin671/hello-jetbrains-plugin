package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project
import com.qingyingliu.hellojebrainsplugin.services.UsagesService

/**
 * Usages命令处理器
 */
class UsagesCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        val trimmedMessage = message.trim()
        return trimmedMessage.startsWith("/usages")
    }
    
    override fun handle(message: String, project: Project): String {
        val usagesService = UsagesService(project)
        
        // 处理 /usages 命令格式
        val slashUsagesMatch = Regex("/usages\\s*(.+)?").find(message)
        if (slashUsagesMatch != null) {
            val name = slashUsagesMatch.groupValues.getOrNull(1)
            if (name != null && name.isNotEmpty()) {
                return usagesService.getUsagesForName(name.trim())
            } else {
                return usagesService.getUsagesHelp()
            }
        }
        
        return usagesService.getUsagesHelp()
    }
    
    override fun getPriority(): Int = 9
} 