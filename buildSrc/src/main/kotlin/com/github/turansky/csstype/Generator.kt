package com.github.turansky.csstype

import com.github.turansky.common.GENERATOR_COMMENT
import java.io.File

fun generateKotlinDeclarations(
    definitionsFile: File,
    sourceDir: File,
) {
    val targetDir = sourceDir.resolve("csstype")
        .also { it.mkdirs() }

    /*
    targetDir.resolve("$name.kt")
        .writeText(fileContent(content))
    */
}

private fun fileContent(
    annotations: String = "",
    body: String,
): String {
    return sequenceOf(
        "// $GENERATOR_COMMENT",
        annotations,
        "package csstype",
        body,
    ).filter { it.isNotEmpty() }
        .joinToString("\n\n")
}
