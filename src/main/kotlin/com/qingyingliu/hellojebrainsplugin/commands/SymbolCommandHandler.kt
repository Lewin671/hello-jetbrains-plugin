package com.qingyingliu.hellojebrainsplugin.commands

import com.intellij.openapi.project.Project
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiManager

/**
 * 符号命令处理器
 */
class SymbolCommandHandler : CommandHandler {
    
    override fun canHandle(message: String): Boolean {
        return message.contains("符号") || message.contains("symbol")
    }
    
    override fun handle(message: String, project: Project): String {
        val editorManager = FileEditorManager.getInstance(project)
        val openFiles = editorManager.selectedFiles

        if (openFiles.isEmpty()) {
            return "当前没有打开任何文件。"
        }

        val psiManager = PsiManager.getInstance(project)
        val builder = StringBuilder("🔖 当前打开文件中的符号：\n\n")

        for (virtualFile in openFiles) {
            val psiFile = psiManager.findFile(virtualFile) ?: continue
            builder.append("文件: ").append(virtualFile.name).append("\n")
            builder.append("  (无符号)\n\n")
        }

        return builder.toString()
    }
    
    override fun getPriority(): Int = 7
} 