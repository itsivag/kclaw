package com.kug.utils

import java.io.File

fun installDir(): File {
    val jarUri = object {}.javaClass.protectionDomain.codeSource.location.toURI()
    return File(jarUri).parentFile.parentFile
}
