package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 问候命令处理器
 */
class GreetingCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        val trimmedMessage = message.trim().lowercase()
        
        // 只匹配完整的问候语，而不是包含问候词的任何消息
        return trimmedMessage == "你好" || 
               trimmedMessage == "hello" ||
               trimmedMessage == "hi" ||
               trimmedMessage == "你好！" ||
               trimmedMessage == "hello!" ||
               trimmedMessage == "hi!"
    }
    
    override fun handle(message: String, project: Project): String {
        return "你好！我是你的AI编程助手 🤖\n\n" +
                "我可以帮助你：\n" +
                "• 分析代码和项目结构\n" +
                "• 提供编程建议\n" +
                "• 回答技术问题\n" +
                "• 查看项目信息\n\n" +
                "试试输入 '/help' 查看更多功能！"
    }
    
    override fun getPriority(): Int = 1
} 