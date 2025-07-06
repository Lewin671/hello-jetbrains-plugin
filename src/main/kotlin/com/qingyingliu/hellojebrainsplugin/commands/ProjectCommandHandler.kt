package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 项目信息命令处理器
 */
class ProjectCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        val trimmedMessage = message.trim()
        return trimmedMessage.startsWith("/project")
    }
    
    override fun handle(message: String, project: Project): String {
        val projectName = project.name
        val projectPath = project.basePath ?: "未知路径"
        val fileCount = getProjectFileCount(project)

        return "📁 项目信息：\n\n" +
                "项目名称：$projectName\n" +
                "项目路径：$projectPath\n" +
                "文件数量：$fileCount 个文件\n\n" +
                "这是一个 JetBrains IDE 插件项目，使用 Kotlin 开发。"
    }
    
    private fun getProjectFileCount(project: Project): Int {
        return try {
            project.baseDir?.children?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    override fun getPriority(): Int = 4
} 