package karakum.csstype

import karakum.common.kebabToCamel

private const val FILTER_FUNCTION = "FilterFunction"

private val PARENT_TYPES = listOf(
    "Filter",
    "BackdropFilter",
)

private const val NUMBER = "Number"
private const val STRING = "String"

private class FilterFactory(
    val name: String,
    val types: Array<out String>,
) {
    companion object {
        val FACTORIES = listOf(
            FilterFactory("blur", NUMBER, LENGTH),
            FilterFactory("brightness", NUMBER, PERCENT),
            FilterFactory("contrast", NUMBER, PERCENT),
            FilterFactory("drop-shadow", STRING),
            FilterFactory("grayscale", NUMBER, PERCENT),
            FilterFactory("hue-rotate", NUMBER, ANGLE),
            FilterFactory("invert", NUMBER, PERCENT),
            FilterFactory("opacity", NUMBER, PERCENT),
            FilterFactory("saturate", NUMBER, PERCENT),
            FilterFactory("sepia", NUMBER, PERCENT),
        )
    }
}

private fun FilterFactory(
    name: String,
    vararg types: String,
): FilterFactory =
    FilterFactory(name, types)

internal fun FilterFunction(): ConversionResult {
    val declarations = sequenceOf(
        """
        sealed external interface $FILTER_FUNCTION:
                ${PARENT_TYPES.joinToString(",\n")}
        """.trimIndent()
    ) + FilterFactory.FACTORIES
        .flatMap { f -> f.types.map { f.name to it } }
        .map { (name, type) ->
            """
            inline fun ${name.kebabToCamel()}(
                value: $type,
            ): $FILTER_FUNCTION =
                "$name(${'$'}value)".unsafeCast<$FILTER_FUNCTION>()
            """.trimIndent()
        } + PARENT_TYPES.map { name ->
        """
            fun $name(
                vararg values: $FILTER_FUNCTION,
            ): $name =
                values.joinToString(" ")
                    .unsafeCast<$name>()
            """.trimIndent()
    }

    return ConversionResult(FILTER_FUNCTION, declarations.joinToString("\n\n"))
}
