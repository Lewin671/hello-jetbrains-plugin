package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 代码帮助命令处理器
 */
class CodeCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        return message.contains("代码") || message.contains("code")
    }
    
    override fun handle(message: String, project: Project): String {
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
    
    override fun getPriority(): Int = 6
} 