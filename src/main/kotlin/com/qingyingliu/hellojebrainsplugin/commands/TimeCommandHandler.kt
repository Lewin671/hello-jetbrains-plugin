package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 时间命令处理器
 */
class TimeCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        val trimmedMessage = message.trim()
        return trimmedMessage.startsWith("/time")
    }
    
    override fun handle(message: String, project: Project): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return "🕐 当前时间：${now.format(formatter)}"
    }
    
    override fun getPriority(): Int = 3
} 