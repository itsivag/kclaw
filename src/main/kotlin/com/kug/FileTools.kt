package com.kug

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import java.io.File

class FileTools : ToolSet {

    private val root = installDir().canonicalFile

    private fun resolve(path: String): File {
        val target = File(root, path).canonicalFile
        require(target.path.startsWith(root.path)) { "Access denied: path is outside install directory" }
        return target
    }

    @Tool
    @LLMDescription("List files and directories at the given path relative to the install directory. Use '.' for the root.")
    fun listFiles(path: String): String {
        val dir = resolve(path)
        if (!dir.exists()) return "Does not exist: $path"
        if (!dir.isDirectory) return "Not a directory: $path"
        val entries = dir.listFiles() ?: return "Empty"
        return entries.joinToString("\n") { if (it.isDirectory) "[dir]  ${it.name}" else "[file] ${it.name}" }
    }

    @Tool
    @LLMDescription("Read the contents of a file at the given path relative to the install directory.")
    fun readFile(path: String): String {
        val file = resolve(path)
        if (!file.exists()) return "Does not exist: $path"
        if (!file.isFile) return "Not a file: $path"
        return file.readText()
    }

    @Tool
    @LLMDescription("Write content to a file at the given path relative to the install directory. Creates the file if it does not exist.")
    fun writeFile(path: String, content: String): String {
        val file = resolve(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
        return "Written: $path"
    }

    @Tool
    @LLMDescription("Create a directory (and any missing parents) at the given path relative to the install directory.")
    fun createDirectory(path: String): String {
        val dir = resolve(path)
        if (dir.exists()) return "Already exists: $path"
        dir.mkdirs()
        return "Created: $path"
    }

    @Tool
    @LLMDescription("Delete a file or directory at the given path relative to the install directory. Directories are deleted recursively.")
    fun delete(path: String): String {
        val target = resolve(path)
        if (!target.exists()) return "Does not exist: $path"
        target.deleteRecursively()
        return "Deleted: $path"
    }
}
