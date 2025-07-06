package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 文件信息命令处理器
 */
class FileCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        val trimmedMessage = message.trim()
        return trimmedMessage.startsWith("/file")
    }
    
    override fun handle(message: String, project: Project): String {
        val projectPath = project.basePath ?: return "无法获取项目路径"
        val rootDir = project.baseDir ?: return "无法获取项目根目录"

        return "📂 项目文件结构：\n\n" +
                "根目录：${rootDir.name}\n" +
                "主要文件类型：\n" +
                "• Kotlin 源文件 (.kt)\n" +
                "• 配置文件 (build.gradle.kts, plugin.xml)\n" +
                "• 资源文件\n\n" +
                "这是一个标准的 IntelliJ Platform 插件项目结构。"
    }
    
    override fun getPriority(): Int = 5
} 