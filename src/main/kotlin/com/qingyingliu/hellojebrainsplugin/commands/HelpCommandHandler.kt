package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 帮助命令处理器
 */
class HelpCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        return message.contains("帮助") || message.contains("help")
    }
    
    override fun handle(message: String, project: Project): String {
        return "📋 可用命令：\n\n" +
                "🔍 项目相关：\n" +
                "• '项目' - 查看当前项目信息\n" +
                "• '文件' - 查看项目文件结构\n\n" +
                "💻 编程相关：\n" +
                "• '代码' - 获取编程帮助\n" +
                "• '符号' - 查看当前打开文件的符号\n" +
                "• 'lint' 或 '检查' - 查看当前文件的代码检查问题\n" +
                "• 'lint 文件路径' - 查看指定文件的代码检查问题\n" +
                "• 'usages 名称' 或 '引用 名称' - 查找指定类或方法的所有引用\n" +
                "• 直接询问编程问题\n\n" +
                "⏰ 其他功能：\n" +
                "• '时间' - 显示当前时间\n" +
                "• '帮助' - 显示此帮助信息\n\n" +
                "💡 提示：你可以用中文或英文与我交流！"
    }
    
    override fun getPriority(): Int = 2
} 