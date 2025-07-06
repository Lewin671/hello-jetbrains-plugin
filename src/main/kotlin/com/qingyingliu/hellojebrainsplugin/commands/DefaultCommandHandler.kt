package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 默认命令处理器 - 处理无法识别的命令
 */
class DefaultCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean = true
    
    override fun handle(message: String, project: Project): String {
        return "我收到了你的消息：\"$message\"\n\n" +
                "虽然我目前还不能完全理解你的具体需求，但我可以：\n" +
                "• 回答编程相关问题\n" +
                "• 提供项目信息\n" +
                "• 解释技术概念\n\n" +
                "请尝试更具体的问题，或者输入 '帮助' 查看可用功能！"
    }
    
    override fun getPriority(): Int = Int.MAX_VALUE // 最低优先级
} 