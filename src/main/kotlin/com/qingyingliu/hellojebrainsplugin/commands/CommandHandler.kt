package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project

/**
 * 命令处理器接口 - 策略模式的基础
 */
interface CommandHandler {
    /**
     * 检查是否可以处理该命令
     */
    fun canHandle(message: String): Boolean
    
    /**
     * 处理命令并返回响应
     */
    fun handle(message: String, project: Project): String
    
    /**
     * 获取命令的优先级（数字越小优先级越高）
     */
    fun getPriority(): Int = 10
} 