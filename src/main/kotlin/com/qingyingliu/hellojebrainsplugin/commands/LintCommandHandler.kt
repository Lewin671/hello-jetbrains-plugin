package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project
import com.qingyingliu.hellojebrainsplugin.services.LintService

/**
 * Lint命令处理器
 */
class LintCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        val trimmedMessage = message.trim()
        return trimmedMessage.startsWith("/lint")
    }
    
    override fun handle(message: String, project: Project): String {
        val lintService = LintService(project)
        
        // 处理 /lint 命令格式
        val slashLintMatch = Regex("/lint\\s*(.+)?").find(message)
        if (slashLintMatch != null) {
            val path = slashLintMatch.groupValues.getOrNull(1)
            if (path != null && path.isNotEmpty()) {
                return lintService.getLintInfoForPath(path.trim())
            } else {
                return lintService.getLintInfo()
            }
        }
        
        return lintService.getLintInfo()
    }
    
    override fun getPriority(): Int = 8
} 