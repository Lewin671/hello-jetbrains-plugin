package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 问候命令处理器
 */
class GreetingCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        return message.contains("你好") || message.contains("hello")
    }
    
    override fun handle(message: String, project: Project): String {
        return "你好！我是你的AI编程助手 🤖\n\n" +
                "我可以帮助你：\n" +
                "• 分析代码和项目结构\n" +
                "• 提供编程建议\n" +
                "• 回答技术问题\n" +
                "• 查看项目信息\n\n" +
                "试试输入 '帮助' 查看更多功能！"
    }
    
    override fun getPriority(): Int = 1
} 