package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 帮助命令处理器
 */
class HelpCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        val trimmedMessage = message.trim()
        return trimmedMessage.startsWith("/help")
    }
    
    override fun handle(message: String, project: Project): String {
        return "📋 可用命令：\n\n" +
                "💻 编程相关：\n" +
                "• '/lint' - 查看当前文件的代码检查问题\n" +
                "• '/lint 文件路径' - 查看指定文件的代码检查问题\n" +
                "• '/usages 名称' - 查找指定类或方法的所有引用\n" +
                "• '/help' - 显示此帮助信息\n\n" +
                "💡 提示：所有命令都使用斜杠前缀格式！"
    }
    
    override fun getPriority(): Int = 2
} 