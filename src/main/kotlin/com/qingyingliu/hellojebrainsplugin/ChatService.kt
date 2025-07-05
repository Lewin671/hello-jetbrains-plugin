package com.qingyingliu.hellojebrainsplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatService(private val project: Project) {
    
    fun processMessage(message: String): String {
        return when {
            message.contains("你好") || message.contains("hello") -> getGreeting()
            message.contains("帮助") || message.contains("help") -> getHelpMessage()
            message.contains("时间") || message.contains("time") -> getCurrentTime()
            message.contains("项目") || message.contains("project") -> getProjectInfo()
            message.contains("文件") || message.contains("file") -> getFileInfo()
            message.contains("代码") || message.contains("code") -> getCodeHelp()
            else -> getDefaultResponse(message)
        }
    }
    
    private fun getGreeting(): String {
        return "你好！我是你的AI编程助手 🤖\n\n" +
               "我可以帮助你：\n" +
               "• 分析代码和项目结构\n" +
               "• 提供编程建议\n" +
               "• 回答技术问题\n" +
               "• 查看项目信息\n\n" +
               "试试输入 '帮助' 查看更多功能！"
    }
    
    private fun getHelpMessage(): String {
        return "📋 可用命令：\n\n" +
               "🔍 项目相关：\n" +
               "• '项目' - 查看当前项目信息\n" +
               "• '文件' - 查看项目文件结构\n\n" +
               "💻 编程相关：\n" +
               "• '代码' - 获取编程帮助\n" +
               "• 直接询问编程问题\n\n" +
               "⏰ 其他功能：\n" +
               "• '时间' - 显示当前时间\n" +
               "• '帮助' - 显示此帮助信息\n\n" +
               "💡 提示：你可以用中文或英文与我交流！"
    }
    
    private fun getCurrentTime(): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return "🕐 当前时间：${now.format(formatter)}"
    }
    
    private fun getProjectInfo(): String {
        val projectName = project.name
        val projectPath = project.basePath ?: "未知路径"
        val fileCount = getProjectFileCount()
        
        return "📁 项目信息：\n\n" +
               "项目名称：$projectName\n" +
               "项目路径：$projectPath\n" +
               "文件数量：$fileCount 个文件\n\n" +
               "这是一个 JetBrains IDE 插件项目，使用 Kotlin 开发。"
    }
    
    private fun getFileInfo(): String {
        val projectPath = project.basePath ?: return "无法获取项目路径"
        val rootDir = project.baseDir
        
        return "📂 项目文件结构：\n\n" +
               "根目录：${rootDir.name}\n" +
               "主要文件类型：\n" +
               "• Kotlin 源文件 (.kt)\n" +
               "• 配置文件 (build.gradle.kts, plugin.xml)\n" +
               "• 资源文件\n\n" +
               "这是一个标准的 IntelliJ Platform 插件项目结构。"
    }
    
    private fun getCodeHelp(): String {
        return "💻 编程帮助：\n\n" +
               "我可以帮助你：\n" +
               "• 分析代码逻辑\n" +
               "• 提供最佳实践建议\n" +
               "• 解释编程概念\n" +
               "• 调试代码问题\n\n" +
               "请直接描述你的编程问题，我会尽力帮助你！\n\n" +
               "例如：\n" +
               "• '如何创建一个新的工具窗口？'\n" +
               "• 'Kotlin 中的扩展函数是什么？'\n" +
               "• '如何注册一个动作？'"
    }
    
    private fun getDefaultResponse(message: String): String {
        return "我收到了你的消息：\"$message\"\n\n" +
               "虽然我目前还不能完全理解你的具体需求，但我可以：\n" +
               "• 回答编程相关问题\n" +
               "• 提供项目信息\n" +
               "• 解释技术概念\n\n" +
               "请尝试更具体的问题，或者输入 '帮助' 查看可用功能！"
    }
    
    private fun getProjectFileCount(): Int {
        return try {
            project.baseDir.children.size
        } catch (e: Exception) {
            0
        }
    }
} 