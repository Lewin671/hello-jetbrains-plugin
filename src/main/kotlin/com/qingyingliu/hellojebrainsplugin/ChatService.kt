package com.qingyingliu.hellojebrainsplugin

import com.intellij.openapi.project.Project
import com.qingyingliu.hellojebrainsplugin.commands.CommandChain

/**
 * 聊天服务 - 重构后的版本
 * 
 * 使用设计模式重构：
 * 1. 策略模式 (Strategy Pattern) - 处理不同类型的命令
 * 2. 责任链模式 (Chain of Responsibility) - 处理命令匹配
 * 3. 单一职责原则 - 每个类只负责一个功能
 * 4. 开闭原则 - 易于扩展新命令
 */
class ChatService(private val project: Project) {
    
    // 使用单例模式管理命令链
    private val commandChain = CommandChain.createDefaultChain()
    
    /**
     * 处理用户消息
     * 
     * @param message 用户输入的消息
     * @return 处理后的响应
     */
    fun processMessage(message: String): String {
        return commandChain.process(message, project)
    }
} 