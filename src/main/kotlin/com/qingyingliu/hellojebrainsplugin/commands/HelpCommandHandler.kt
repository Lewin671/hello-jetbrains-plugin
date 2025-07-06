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
                "🔍 项目相关：\n" +
                "• '/project' - 查看当前项目信息\n" +
                "• '/file' - 查看项目文件结构\n\n" +
                "💻 编程相关：\n" +
                "• '/code' - 获取编程帮助\n" +
                "• '/symbol' - 查看当前打开文件的符号\n" +
                "• '/lint' - 查看当前文件的代码检查问题\n" +
                "• '/lint 文件路径' - 查看指定文件的代码检查问题\n" +
                "• '/usages 名称' - 查找指定类或方法的所有引用\n" +
                "• 直接询问编程问题\n\n" +
                "⏰ 其他功能：\n" +
                "• '/time' - 显示当前时间\n" +
                "• '/help' - 显示此帮助信息\n\n" +
                "💡 提示：所有命令都使用斜杠前缀格式！"
    }
    
    override fun getPriority(): Int = 2
} 