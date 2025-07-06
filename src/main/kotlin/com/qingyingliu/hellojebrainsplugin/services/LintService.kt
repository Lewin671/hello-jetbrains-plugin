package com.qingyingliu.hellojebrainsplugin.services

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.util.Processors
import java.io.File
import com.intellij.openapi.application.ReadAction

/**
 * 代码检查服务
 */
class LintService(private val project: Project) {
    
    /**
     * 获取当前打开文件的代码检查信息
     */
    fun getLintInfo(): String {
        val editorManager = FileEditorManager.getInstance(project)
        val openFiles = editorManager.selectedFiles

        if (openFiles.isEmpty()) {
            return "当前没有打开任何文件。请先打开一个文件，然后使用 'lint' 命令查看代码检查问题。\n\n" +
                   "💡 提示：你也可以指定文件路径，例如：\n" +
                   "• 'lint /path/to/file.kt'\n" +
                   "• '检查 src/main/kotlin/MyFile.kt'\n" +
                   "• '问题 ChatService.kt'"
        }

        val builder = StringBuilder("🔍 代码检查问题：\n\n")
        for (virtualFile in openFiles) {
            // 在 ReadAction 中执行 PSI 访问相关逻辑，避免线程违规
            val result = com.intellij.openapi.application.ReadAction.compute<String, RuntimeException> {
                analyzeFileForLint(virtualFile)
            }
            builder.append(result)
        }
        return builder.toString()
    }
    
    /**
     * 获取指定路径文件的代码检查信息
     */
    fun getLintInfoForPath(filePath: String): String {
        val builder = StringBuilder("🔍 代码检查问题：\n\n")
        
        // 处理相对路径和绝对路径
        val resolvedPath = when {
            filePath.startsWith("/") -> filePath // 绝对路径
            filePath.startsWith("~") -> filePath.replaceFirst("~", System.getProperty("user.home")) // 用户主目录
            else -> {
                // 相对路径，尝试从项目根目录解析
                val projectPath = project.basePath
                if (projectPath != null) {
                    "$projectPath/$filePath"
                } else {
                    filePath
                }
            }
        }

        val file = File(resolvedPath)
        if (!file.exists()) {
            return "❌ 文件不存在：$resolvedPath\n\n" +
                   "💡 提示：请检查文件路径是否正确，或者使用绝对路径。"
        }

        if (!file.isFile) {
            return "❌ 指定路径不是文件：$resolvedPath\n\n" +
                   "💡 提示：请指定一个具体的文件路径，而不是目录。"
        }

        // 获取 VirtualFile
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
        if (virtualFile == null) {
            return "❌ 无法访问文件：$resolvedPath\n\n" +
                   "💡 提示：请确保文件存在且有读取权限。"
        }

        val result = com.intellij.openapi.application.ReadAction.compute<String, RuntimeException> {
            analyzeFileForLint(virtualFile)
        }
        builder.append(result)
        return builder.toString()
    }
    
    private fun analyzeFileForLint(virtualFile: VirtualFile): String {
        val builder = StringBuilder()
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        
        if (psiFile == null) {
            builder.append("📄 文件: ${virtualFile.name}\n")
            builder.append("  ❌ 无法解析文件内容\n\n")
            return builder.toString()
        }

        builder.append("📄 文件: ${virtualFile.name}\n")
        builder.append("📍 路径: ${virtualFile.path}\n")

        // 获取编辑器实例
        val editorManager = FileEditorManager.getInstance(project)
        val editor = editorManager.getSelectedTextEditor()
        
        if (editor == null) {
            // 如果文件没有在编辑器中打开，我们需要创建一个临时的编辑器
            val document = psiFile.viewProvider.document
            if (document == null) {
                builder.append("  ❌ 无法获取文档内容\n\n")
                return builder.toString()
            }
            
            return analyzeDocumentForLint(document, virtualFile.name, virtualFile.path)
        }

        return analyzeDocumentForLint(editor.document, virtualFile.name, virtualFile.path)
    }
    
    private fun analyzeDocumentForLint(document: Document, fileName: String, filePath: String): String {
        val builder = StringBuilder()
        builder.append("📄 文件: $fileName\n")
        builder.append("📍 路径: $filePath\n")

        val highlights = mutableListOf<HighlightInfo>()

        // 获取错误级别的问题
        DaemonCodeAnalyzerEx.processHighlights(
            document,
            project,
            HighlightSeverity.ERROR,
            document.getLineStartOffset(0),
            document.getLineEndOffset(document.lineCount - 1),
            Processors.cancelableCollectProcessor(highlights)
        )

        // 获取警告级别的问题
        val warnings = mutableListOf<HighlightInfo>()
        DaemonCodeAnalyzerEx.processHighlights(
            document,
            project,
            HighlightSeverity.WARNING,
            document.getLineStartOffset(0),
            document.getLineEndOffset(document.lineCount - 1),
            Processors.cancelableCollectProcessor(warnings)
        )

        // 获取弱警告级别的问题
        val weakWarnings = mutableListOf<HighlightInfo>()
        DaemonCodeAnalyzerEx.processHighlights(
            document,
            project,
            HighlightSeverity.WEAK_WARNING,
            document.getLineStartOffset(0),
            document.getLineEndOffset(document.lineCount - 1),
            Processors.cancelableCollectProcessor(weakWarnings)
        )

        val totalProblems = highlights.size + warnings.size + weakWarnings.size

        if (totalProblems == 0) {
            builder.append("  ✅ 没有发现代码问题\n\n")
        } else {
            builder.append("  📊 总计发现 $totalProblems 个问题：\n")
            builder.append("    • 错误: ${highlights.size} 个\n")
            builder.append("    • 警告: ${warnings.size} 个\n")
            builder.append("    • 弱警告: ${weakWarnings.size} 个\n\n")

            // 显示错误详情
            if (highlights.isNotEmpty()) {
                builder.append("  ❌ 错误详情：\n")
                highlights.forEachIndexed { index, highlight ->
                    val line = document.getLineNumber(highlight.startOffset) + 1
                    val column = highlight.startOffset - document.getLineStartOffset(line - 1) + 1
                    val description = highlight.description ?: "未知错误"
                    val tooltip = highlight.toolTip ?: description
                    
                    builder.append("    ${index + 1}. 第 ${line} 行，第 ${column} 列\n")
                    builder.append("       描述: $description\n")
                    if (tooltip != description) {
                        builder.append("       详情: $tooltip\n")
                    }
                    builder.append("\n")
                }
            }
        }

        return builder.toString()
    }
} 