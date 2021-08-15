package com.github.turansky.react

internal fun convertUnion(
    name: String,
    source: String,
): ConversionResult? {
    if ("<" in name)
        return null

    if (" | '" !in source)
        return null

    val constMap = source.removePrefix("\n")
        .trimIndent()
        .splitToSequence("\n")
        .map { it.removePrefix("| ") }
        .filter { it != "(string & {})" }
        .map { it.removeSurrounding("'") }
        .associateBy { enumConstant(it) }

    val jsName = constMap.asSequence()
        .joinToString(
            separator = ", ",
            prefix = "@JsName(\"\"\"({",
            postfix = "})\"\"\")",
        ) { (key, value) ->
            "$key: '$value'"
        }

    val constantNames = constMap.keys
        .joinToString("") { "$it,\n" }

    val body = """
        @Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
        // language=JavaScript
        $jsName
        external enum class $name {
            $constantNames
            ;
        }
    """.trimIndent()

    return ConversionResult(name, body)
}

private fun enumConstant(
    value: String,
): String =
    when {
        value == "" -> "none"
        "-" !in value -> value
        else -> value.kebabToCamel()
    }
