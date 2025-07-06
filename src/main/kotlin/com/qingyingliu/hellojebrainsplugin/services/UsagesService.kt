package com.qingyingliu.hellojebrainsplugin.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.openapi.application.ReadAction

/**
 * 引用查找服务
 */
class UsagesService(private val project: Project) {
    
    /**
     * 获取指定名称（类名或方法名）的所有引用
     */
    fun getUsagesForName(name: String): String {
        return ReadAction.compute<String, RuntimeException> {
            val builder = StringBuilder("🔍 查找 '$name' 的引用：\n\n")
            
            try {
                // 1. 首先尝试查找类定义
                val psiClass = findClassByName(name)
                
                if (psiClass != null) {
                    // 检查是否有多个匹配的类
                    val allMatchingClasses = findClassesByNameInProject(name)
                    
                    // 找到类，显示类信息
                    builder.append("📋 类信息：\n")
                    builder.append("• 类名: ${psiClass.name}\n")
                    builder.append("• 全限定名: ${psiClass.qualifiedName}\n")
                    builder.append("• 包名: ${psiClass.qualifiedName?.substringBeforeLast('.') ?: "未知"}\n")
                    builder.append("• 文件: ${psiClass.containingFile?.name ?: "未知"}\n")
                    builder.append("• 位置: ${psiClass.containingFile?.virtualFile?.path ?: "未知"}\n\n")
                    
                    // 如果找到多个类，显示所有选项
                    if (allMatchingClasses.size > 1) {
                        builder.append("🔍 找到多个匹配的类：\n")
                        allMatchingClasses.forEachIndexed { index, cls ->
                            builder.append("  ${index + 1}. ${cls.qualifiedName} (在 ${cls.containingFile?.name ?: "未知文件"})\n")
                        }
                        builder.append("\n💡 提示：当前显示第一个类的引用。如需查看其他类，请使用完整包名。\n\n")
                    }
                    
                    // 查找类的所有引用
                    val usages = findUsages(psiClass)
                    
                    if (usages.isEmpty()) {
                        builder.append("✅ 没有找到任何引用\n\n")
                        builder.append("💡 说明：\n")
                        builder.append("• 这个类可能没有被使用\n")
                        builder.append("• 或者引用在注释或字符串中（不会被索引）\n")
                        builder.append("• 或者引用在项目范围之外\n\n")
                    } else {
                        builder.append("📊 找到 ${usages.size} 个引用：\n\n")
                        displayUsages(usages, builder)
                    }
                    
                    return@compute builder.toString()
                }
                
                // 2. 如果没有找到类，尝试查找方法
                val methods = findMethodsByName(name)
                
                if (methods.isNotEmpty()) {
                    builder.append("📋 方法信息：\n")
                    builder.append("• 方法名: $name\n")
                    builder.append("• 找到 ${methods.size} 个方法定义\n\n")
                    
                    // 查找所有方法的引用
                    val allUsages = mutableListOf<PsiElement>()
                    methods.forEach { method ->
                        val methodUsages = findUsages(method)
                        allUsages.addAll(methodUsages)
                    }
                    
                    if (allUsages.isEmpty()) {
                        builder.append("✅ 没有找到任何引用\n\n")
                        builder.append("💡 说明：\n")
                        builder.append("• 这些方法可能没有被调用\n")
                        builder.append("• 或者调用在注释或字符串中（不会被索引）\n")
                        builder.append("• 或者调用在项目范围之外\n\n")
                    } else {
                        builder.append("📊 找到 ${allUsages.size} 个引用：\n\n")
                        displayUsages(allUsages, builder)
                    }
                    
                    return@compute builder.toString()
                }
                
                // 3. 如果既没有找到类也没有找到方法
                builder.append("❌ 未找到类或方法 '$name'\n\n")
                builder.append("💡 提示：\n")
                builder.append("• 请检查名称是否正确（区分大小写）\n")
                builder.append("• 如果是内部类，请使用 'OuterClass.InnerClass' 格式\n")
                builder.append("• 如果是全限定类名，请包含包名\n")
                builder.append("• 如果是方法，请确保方法名正确\n")
                builder.append("• 例如：'java.lang.String'、'MyClass'、'processMessage'\n\n")
                
            } catch (e: Exception) {
                builder.append("❌ 查找过程中发生错误：${e.message}\n\n")
                builder.append("💡 可能的原因：\n")
                builder.append("• 项目索引可能不完整\n")
                builder.append("• 名称格式不正确\n")
                builder.append("• 项目配置问题\n\n")
                e.printStackTrace()
            }
            
            builder.toString()
        }
    }
    
    /**
     * 显示 usages 命令的帮助信息
     */
    fun getUsagesHelp(): String {
        return "🔍 引用查找命令帮助：\n\n" +
                "📝 用法：\n" +
                "• 'usages 名称' - 查找指定类或方法的所有引用\n" +
                "• '引用 名称' - 查找指定类或方法的所有引用\n" +
                "• '用法 名称' - 查找指定类或方法的所有引用\n\n" +
                "💡 示例：\n" +
                "• 'usages String' - 查找 java.lang.String 的引用\n" +
                "• '引用 ChatService' - 查找 ChatService 类的引用（无需包名）\n" +
                "• '用法 processMessage' - 查找 processMessage 方法的引用\n" +
                "• 'usages java.util.List' - 查找 List 接口的引用\n\n" +
                "📋 支持的名称格式：\n" +
                "• 类名：'MyClass'（自动搜索项目内所有匹配的类）、'com.example.MyClass'、'OuterClass.InnerClass'\n" +
                "• 方法名：'processMessage'、'getUsagesForName'、'findClassByName'\n" +
                "• 标准库类：'String', 'List', 'Map' 等\n\n" +
                "🎯 智能搜索：\n" +
                "• 输入简单类名时，会自动搜索项目内所有匹配的类\n" +
                "• 如果找到多个类，会显示所有选项并选择第一个\n" +
                "• 如需指定特定类，请使用完整包名\n\n" +
                "⚠️ 注意事项：\n" +
                "• 名称区分大小写\n" +
                "• 只查找项目范围内的引用\n" +
                "• 注释和字符串中的引用不会被包含\n"
    }
    
    /**
     * 根据类名查找 PsiClass
     */
    private fun findClassByName(className: String): PsiClass? {
        val scope = GlobalSearchScope.allScope(project)
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        
        // 如果包含包名，直接查找
        if (className.contains(".")) {
            return javaPsiFacade.findClass(className, scope)
        }
        
        // 如果不包含包名，先在项目范围内搜索
        val projectClasses = findClassesByNameInProject(className)
        
        if (projectClasses.isNotEmpty()) {
            // 如果找到多个类，选择第一个（通常是最相关的）
            return projectClasses.first()
        }
        
        // 如果项目内没找到，尝试标准库类
        val standardClasses = listOf(
            "java.lang.$className",
            "java.util.$className",
            "java.io.$className",
            "java.math.$className",
            "java.text.$className",
            "java.time.$className"
        )
        
        for (fullClassName in standardClasses) {
            val psiClass = javaPsiFacade.findClass(fullClassName, scope)
            if (psiClass != null) {
                return psiClass
            }
        }
        
        return null
    }
    
    /**
     * 在项目范围内根据类名查找所有匹配的类
     */
    private fun findClassesByNameInProject(className: String): List<PsiClass> {
        return ReadAction.compute<List<PsiClass>, RuntimeException> {
            val classes = mutableListOf<PsiClass>()
            val psiManager = PsiManager.getInstance(project)
            
            try {
                // 遍历项目中的所有文件来查找类
                val rootDir = project.baseDir ?: return@compute emptyList<PsiClass>()
                collectClassesFromDirectory(rootDir, className, classes, psiManager)
                classes
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    /**
     * 递归遍历目录查找类
     */
    private fun collectClassesFromDirectory(directory: VirtualFile, className: String, classes: MutableList<PsiClass>, psiManager: PsiManager) {
        for (child in directory.children) {
            if (child.isDirectory) {
                collectClassesFromDirectory(child, className, classes, psiManager)
            } else if (child.extension in listOf("java", "kt", "groovy")) {
                val psiFile = psiManager.findFile(child)
                if (psiFile != null) {
                    collectClassesFromFile(psiFile, className, classes)
                }
            }
        }
    }
    
    /**
     * 从单个文件中查找类
     */
    private fun collectClassesFromFile(psiFile: PsiFile, className: String, classes: MutableList<PsiClass>) {
        psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is PsiClass && element.name == className) {
                    classes.add(element)
                }
                super.visitElement(element)
            }
        })
    }
    
    /**
     * 根据方法名查找所有匹配的方法
     */
    private fun findMethodsByName(methodName: String): List<PsiMethod> {
        return ReadAction.compute<List<PsiMethod>, RuntimeException> {
            val methods = mutableListOf<PsiMethod>()
            
            try {
                // 遍历项目中的所有类来查找方法
                val rootDir = project.baseDir ?: return@compute emptyList<PsiMethod>()
                collectMethodsFromDirectory(rootDir, methodName, methods, PsiManager.getInstance(project))
                methods
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    /**
     * 递归遍历目录查找方法
     */
    private fun collectMethodsFromDirectory(directory: VirtualFile, methodName: String, methods: MutableList<PsiMethod>, psiManager: PsiManager) {
        for (child in directory.children) {
            if (child.isDirectory) {
                collectMethodsFromDirectory(child, methodName, methods, psiManager)
            } else if (child.extension in listOf("java", "kt", "groovy")) {
                val psiFile = psiManager.findFile(child)
                if (psiFile != null) {
                    collectMethodsFromFile(psiFile, methodName, methods)
                }
            }
        }
    }
    
    /**
     * 从单个文件中查找方法
     */
    private fun collectMethodsFromFile(psiFile: PsiFile, methodName: String, methods: MutableList<PsiMethod>) {
        psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is PsiMethod && element.name == methodName) {
                    methods.add(element)
                }
                super.visitElement(element)
            }
        })
    }
    
    /**
     * 查找指定 PSI 元素的所有引用
     */
    private fun findUsages(element: PsiElement): List<PsiElement> {
        return ReadAction.compute<List<PsiElement>, RuntimeException> {
            val usages = mutableListOf<PsiElement>()
            
            val targetName = when (element) {
                is PsiClass -> element.name
                is PsiMethod -> element.name
                is PsiField -> element.name
                is PsiVariable -> element.name
                else -> element.text
            }
            
            if (targetName == null) {
                return@compute emptyList<PsiElement>()
            }
            
            try {
                val psiManager = PsiManager.getInstance(project)
                val rootDir = project.baseDir ?: return@compute emptyList<PsiElement>()
                collectUsagesFromDirectory(rootDir, targetName, usages, psiManager)
                usages
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    /**
     * 递归遍历目录查找引用
     */
    private fun collectUsagesFromDirectory(directory: VirtualFile, targetName: String, usages: MutableList<PsiElement>, psiManager: PsiManager) {
        for (child in directory.children) {
            if (child.isDirectory) {
                collectUsagesFromDirectory(child, targetName, usages, psiManager)
            } else if (child.extension in listOf("java", "kt", "groovy")) {
                val psiFile = psiManager.findFile(child)
                if (psiFile != null) {
                    collectUsagesFromFile(psiFile, targetName, usages)
                }
            }
        }
    }
    
    /**
     * 从单个文件中查找引用
     */
    private fun collectUsagesFromFile(psiFile: PsiFile, targetName: String, usages: MutableList<PsiElement>) {
        psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                // 检查元素是否是标识符且名称匹配
                if (element is PsiIdentifier && element.text == targetName) {
                    // 确保这不是定义本身
                    val parent = element.parent
                    if (parent is PsiNameIdentifierOwner && parent.nameIdentifier == element) {
                        // 这是定义，不是引用，跳过
                        return
                    }
                    usages.add(element)
                }
                super.visitElement(element)
            }
        })
    }
    
    /**
     * 获取元素的行号
     */
    private fun getElementLineNumber(element: PsiElement): Int {
        return try {
            val document = element.containingFile?.viewProvider?.document
            if (document != null) {
                document.getLineNumber(element.textRange.startOffset) + 1
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取元素的列号
     */
    private fun getElementColumnNumber(element: PsiElement): Int {
        return try {
            val document = element.containingFile?.viewProvider?.document
            if (document != null) {
                val lineStart = document.getLineStartOffset(document.getLineNumber(element.textRange.startOffset))
                element.textRange.startOffset - lineStart + 1
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取元素的上下文信息
     */
    private fun getElementContext(element: PsiElement): String {
        return try {
            val parent = element.parent
            return when {
                parent is PsiClass -> "类定义中"
                parent is PsiMethod -> "方法 '${parent.name}' 中"
                parent is PsiField -> "字段 '${parent.name}' 中"
                parent is PsiVariable -> "变量 '${parent.name}' 中"
                parent is PsiPackage -> "包 '${parent.name}' 中"
                else -> "代码中"
            }
        } catch (e: Exception) {
            "代码中"
        }
    }
    
    /**
     * 显示引用信息
     */
    private fun displayUsages(usages: List<PsiElement>, builder: StringBuilder) {
        // 按文件分组显示引用
        val usagesByFile = usages.groupBy { it.containingFile }
        
        usagesByFile.forEach { (file, fileUsages) ->
            builder.append("📄 文件: ${file?.name ?: "未知文件"}\n")
            builder.append("📍 路径: ${file?.virtualFile?.path ?: "未知路径"}\n")
            
            fileUsages.forEachIndexed { index, element ->
                val line = getElementLineNumber(element)
                val column = getElementColumnNumber(element)
                val context = getElementContext(element)
                
                builder.append("  ${index + 1}. 第 ${line} 行，第 ${column} 列\n")
                builder.append("     上下文: $context\n")
                builder.append("     内容: ${element.text.take(100)}${if (element.text.length > 100) "..." else ""}\n\n")
            }
        }
    }
} 