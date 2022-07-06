package karakum.table

import java.io.File

internal data class ConversionResult(
    val name: String,
    val body: String,
)

private val EXCLUDED_ITEMS = setOf(
    "IfDefined",
    "memo",
    "Overwrite",

    "NoInfer",
    "PartialKeys",
    "RequiredKeys",
    "UnionToIntersection",

    "isFunction",
    "makeStateUpdater",
)

internal fun convertDefinitions(
    definitionFile: File,
): Sequence<ConversionResult> =
    definitionFile.readText()
        .substringBefore("\n\nexport {")
        .splitToSequence("\ndeclare ")
        .drop(1)
        .map { it.removeSuffix(";") }
        .filter { " = keyof typeof " !in it }
        .map { convertDefinition(it) }
        .filter { it.name !in EXCLUDED_ITEMS }

private fun convertDefinition(
    source: String,
): ConversionResult {
    val type = source.substringBefore(" ")
    val body = source.substringAfter(" ")

    return when (type) {
        "const" -> convertConst(body)
        "function" -> convertFunction(body)
        "type" -> convertType(body)
        else -> TODO()
    }
}

private fun convertConst(
    source: String,
): ConversionResult {
    val name = source.substringBefore(":")
    if (name == "createRow") {
        val newSource = source
            .replaceFirst(": ", "")
            .replace(" => ", ": ")

        return convertFunction(newSource)
    }

    val type = if (": {" in source) "object" else "val"

    val body = source.replace(": {", " {")
        .replace("\n    ", "\n    val ")
        .replace("<any>", "<*>")
        .replace("number", "Double")

    val content = "external $type $body"
    return ConversionResult(name, content)
}

private fun convertFunction(
    source: String,
): ConversionResult {
    val name = source
        .substringBefore(" ")
        .substringBefore(":")
        .substringBefore("<")
        .substringBefore("(")

    var body = source.removePrefix(name)
        .replaceFirst("(", " $name(")
        .replace(" extends ", " : ")
        .replace("?: {\n    initialSync: boolean;\n}", ": dynamic = definedExternally /* { initialSync: boolean } */")
        .replace(
            "headerFamily?: 'center' | 'left' | 'right'",
            "headerFamily: String = definedExternally /* 'center' | 'left' | 'right' */"
        )
        .replace(": boolean | 'some' | 'all'", ": Any /* Boolean | 'some' | 'all' */")
        .replace("Record<string, boolean>", "Record<String, Boolean>")
        .replace("Map<any, number>", "Record<Any, Int> /* JS Map */")
        .replace(" => void", " -> Unit")
        .replace(" => ", " -> ")
        .replace(": string[]", ": ReadonlyArray<String>")
        .replace(" TNode[]", " ReadonlyArray<TNode>")
        .replace(": Column<TData>[]", ": ReadonlyArray<Column<TData>>")
        .replace(": HeaderGroup<TData>[]", ": ReadonlyArray<HeaderGroup<TData>>")
        .replace("undefined | [number, number]", "JsPair<Int, Int>?")
        .replace("?: Column<TData>", ": Column<TData> = definedExternally")
        .replace(": TData | undefined", ": TData?")
        .replace("?: Row<TData>[] | undefined", ": ReadonlyArray<Row<TData>>? = definedExternally")
        .replace(": string", ": String")
        .replace(": number", ": Int")
        .replace(": boolean", ": Boolean")
        .replace("?: GroupingColumnMode", ": GroupingColumnMode = definedExternally")
        .replace("?: FilterFn<TData>", ": FilterFn<TData> = definedExternally")
        .replace("?: any", ": Any = definedExternally")
        .replace(": void", "")

    // TODO: use result interface instead
    if ("): {" in body)
        body = body.replace("): {", "): Any /* {") + " */"

    return ConversionResult(name, "external fun " + body)
}

private fun convertType(
    source: String,
): ConversionResult =
    if (" = {\n" in source) {
        convertInterface(source)
    } else {
        convertTypealias(source)
    }

private fun convertTypealias(
    source: String,
): ConversionResult {
    val name = source
        .substringBefore(" ")
        .substringBefore(":")
        .substringBefore("<")
        .substringBefore("(")

    var declaration = source.substringBefore(" = ")
        .replace(" extends ", " : ")

    var body = source.substringAfter(" = ")
        .replace(" => ", " -> ")

    if (body == "{}")
        return ConversionResult(name, "external interface $declaration")

    if (" | " in body) {
        declaration = declaration.replace(": object>", "/* : Any */>")

        return ConversionResult(name, "external interface $declaration /* $body */")
    }

    if ("&" in body) {
        if (body.startsWith("CoreColumnDefBase<TData> & {\n")) {
            var members = convertMembers(body.substringAfter("CoreColumnDefBase<TData> & {\n"))
            if (name == "CoreColumnDefDisplayWithStringHeader")
                members = members.replace("var header: String", "override var header: dynamic /* String */")

            return ConversionResult(name, "external interface $declaration : CoreColumnDefBase<TData> {\n${members}\n}")
        }

        val interfaceBody = body
            .removeSurrounding("Partial<", ">")
            .replace(" & ", ",\n")

        return ConversionResult(name, "external interface $declaration :\n$interfaceBody")
    }

    declaration = declaration
        .replace(": RowData>", "/* : RowData */>")

    body = body
        .replace("(...args: any) -> any", "Function<Unit>")
        .replace("string[]", "ReadonlyArray<String>")
        .replace("ColumnFilter[]", "ReadonlyArray<ColumnFilter>")
        .replace("ColumnSort[]", "ReadonlyArray<ColumnSort>")
        .replace("Row<TData>[]", "ReadonlyArray<Row<TData>>")
        .replace("column?: Column<TData>", "column: Column<TData>?")
        .replace("string", "String")
        .replace("boolean", "Boolean")
        .replace("number", "Int")
        .replace(": any", ": Any")
        .replace(" -> any", " -> Any")
        .replace(" -> unknown", " -> Any")
        .replace("<any>", "<*>")
        .replace(" -> void", " -> Unit")

    if (body == "Partial<UnionToIntersection<CoreColumnDef<TData>>>")
        body = "CoreColumnDef<TData> /* Partial<UnionToIntersection<CoreColumnDef<TData>>> */"

    return ConversionResult(name, "typealias $declaration = $body")
}

private fun convertInterface(
    source: String,
): ConversionResult {
    val declaration = source.substringBefore(" = {")
        .replace(" extends ", " : ")
    val name = declaration.substringBefore("<")

    if (name == "RowValues")
        return ConversionResult(name, "typealias $name = Record<String, Any>")

    val body = "{\n" + convertMembers(source.substringAfter(" = {")) + "\n}\n"
    return ConversionResult(name, "external interface $declaration$body")
}

private fun convertMembers(
    source: String,
): String {
    val content = source
        .removeSuffix("\n")
        .substringBeforeLast("\n}")
        .trimIndent()

    if ("\ngetContext: () => {\n" in content) {
        val contextBody = content
            .substringAfter("\ngetContext: () => {\n")
            .substringBefore("\n}")

        return sequenceOf(
            convertMembers(content.replace("() => {\n$contextBody\n}", "() -> Context<TData>")),
            "interface Context<TData : RowData> {\n" + convertMembers(contextBody) + "\n}\n",
        ).joinToString("\n\n")
    }

    return content
        .splitToSequence("\n")
        .filter { !it.startsWith("_") }
        .map { it.removeSuffix(";") }
        .map { convertMember(it) }
        .joinToString("\n")
}

private fun convertMember(
    source: String,
): String {
    if (source.startsWith("("))
        return "    // TODO: support invoke\n    /* $source */"

    val optional = source.substringBefore(": ")
        .endsWith("?")

    val name = source.substringBefore(": ")
        .removeSuffix("?")

    var type = source.substringAfter(": ")
        .let { kotlinType(it, name) }

    if (optional) {
        type = if (type.startsWith("(")) {
            "($type)?"
        } else "$type?"
    }

    return "var $name: $type"
}
