package karakum.typescript

// language=Kotlin
private val JS_ITERATOR_BODY = """
external interface JsIteratorResult<out T> {
    val value: T
    val done: Boolean
}

/** ES6 Iterator type. */
external interface JsIterator<out T> {
    fun next(): JsIteratorResult<T>
}

operator fun <T> JsIterator<T>.iterator(): Iterator<T> =
    JsIteratorAdapter(this)

private class JsIteratorAdapter<T>(
    private val source: JsIterator<T>,
) : Iterator<T> {
    private var lastResult = source.next()

    override fun next(): T {
        check(!lastResult.done)
        val value = lastResult.value

        lastResult = source.next()
        return value
    }

    override fun hasNext(): Boolean =
        !lastResult.done
}
""".trimIndent()

internal fun arrayHelpers(): Sequence<ConversionResult> =
    sequenceOf(
        ConversionResult(
            name = "ReadonlyArray",
            body = "typealias ReadonlyArray<T> = Array<out T>"
        ),
        ConversionResult(
            name = "JsIterator",
            body = JS_ITERATOR_BODY,
        ),
    )
