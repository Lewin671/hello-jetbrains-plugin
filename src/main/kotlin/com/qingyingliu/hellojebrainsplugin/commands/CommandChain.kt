package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 命令处理链 - 责任链模式
 */
class CommandChain(private val handlers: List<CommandHandler>) {
    
    /**
     * 处理消息，按优先级顺序尝试每个处理器
     */
    fun process(message: String, project: Project): String {
        val cleanMessage = message.trim().replace(Regex("[\\r\\n\\t]+"), " ")
        
        // 按优先级排序处理器
        val sortedHandlers = handlers.sortedBy { it.getPriority() }
        
        for (handler in sortedHandlers) {
            if (handler.canHandle(cleanMessage)) {
                return handler.handle(cleanMessage, project)
            }
        }
        
        // 如果没有处理器能处理，返回默认响应
        return DefaultCommandHandler().handle(cleanMessage, project)
    }
    
    companion object {
        /**
         * 创建默认的命令链
         */
        fun createDefaultChain(): CommandChain {
            val handlers = listOf(
                HelpCommandHandler(),
                LintCommandHandler(),
                UsagesCommandHandler()
            )
            return CommandChain(handlers)
        }
    }
} 