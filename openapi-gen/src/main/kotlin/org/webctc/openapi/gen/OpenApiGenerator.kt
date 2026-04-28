package org.webctc.openapi.gen

import java.io.File

private data class RouteRegistration(val basePath: String, val routerClass: String)

private data class RouteDoc(
    val routerClass: String,
    val method: String,
    val path: String,
    val summary: String,
    val response: String,
    val request: String,
    val query: List<String>,
    val authenticated: Boolean,
    val websocket: Boolean,
)

private data class SchemaModel(val name: String, val properties: List<SchemaProperty>)

private data class SchemaProperty(
    val name: String,
    val type: String,
    val required: Boolean,
)

private data class EnumModel(val name: String, val values: List<String>)

fun main(args: Array<String>) {
    require(args.size >= 3) {
        "Usage: OpenApiGenerator <routeSourceRoot> <webCtcCoreFile> <outputFile> [schemaSourceRoot...]"
    }

    val sourceRoot = File(args[0])
    val webCtcCoreFile = File(args[1])
    val outputFile = File(args[2])
    val schemaRoots = args.drop(3).map(::File).ifEmpty { listOf(sourceRoot) }

    val registrations = parseRegistrations(webCtcCoreFile)
    val docs = parseRouteDocsFromRoot(sourceRoot)
    val schemaIndex = parseSchemas(schemaRoots)
    val byRouter = docs.groupBy { it.routerClass }
    val routes = registrations.flatMap { registration ->
        byRouter[registration.routerClass].orEmpty().map { doc ->
            doc.copy(path = joinPaths(registration.basePath, doc.path))
        }
    }.sortedWith(compareBy<RouteDoc> { it.path }.thenBy { it.method })

    outputFile.parentFile.mkdirs()
    outputFile.writeText(renderOpenApi(routes, schemaIndex))
}

private data class SchemaIndex(
    val dataClasses: Map<String, SchemaModel>,
    val enums: Map<String, EnumModel>,
)

private fun parseRegistrations(file: File): List<RouteRegistration> {
    val text = file.readText()
    val regex = Regex("""RouterManager\.registerRouter\("([^"]+)",\s*([A-Za-z0-9_]+)\(\)\)""")
    return regex.findAll(text)
        .map { RouteRegistration(it.groupValues[1], it.groupValues[2]) }
        .toList()
}

private fun parseRouteDocsFromRoot(sourceRoot: File): List<RouteDoc> {
    return sourceRoot.walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .flatMap { parseRouteDocsFromFile(it).asSequence() }
        .toList()
}

private fun parseRouteDocsFromFile(file: File): List<RouteDoc> {
    val text = file.readText()
    val docs = mutableListOf<RouteDoc>()
    var index = 0
    while (true) {
        val classMatch = Regex("""class\s+([A-Za-z0-9_]+)""").find(text, index) ?: break
        val routerClass = classMatch.groupValues[1]
        val classBodyStart = text.indexOf('{', classMatch.range.last)
        if (classBodyStart < 0) break
        val classBodyEnd = findMatchingBrace(text, classBodyStart)
        if (classBodyEnd < 0) break
        docs += parseOpenApiRoutes(routerClass, text.substring(classBodyStart + 1, classBodyEnd))
        index = classBodyEnd + 1
    }
    return docs
}

private fun parseOpenApiRoutes(routerClass: String, body: String): List<RouteDoc> {
    val docs = mutableListOf<RouteDoc>()
    var index = 0
    while (true) {
        val annotationIndex = body.indexOf("@OpenApiRoute", index)
        if (annotationIndex < 0) break
        val annotationOpenParen = body.indexOf('(', annotationIndex)
        if (annotationOpenParen < 0) break
        val annotationCloseParen = findMatchingParen(body, annotationOpenParen)
        if (annotationCloseParen < 0) break
        val values = parseNamedArguments(body.substring(annotationOpenParen + 1, annotationCloseParen))
        val tail = body.substring(annotationCloseParen + 1)
        val routeMatch = Regex("""^\s*(get|post|put|patch|delete|webSocket)\s*([({])""")
            .find(tail)
        if (routeMatch == null) {
            index = annotationCloseParen + 1
            continue
        }
        val routeOpenIndex = annotationCloseParen + 1 + routeMatch.range.last
        val hasArguments = routeMatch.groupValues[2] == "("
        val routeBody = if (hasArguments) {
            val closeParen = findMatchingParen(body, routeOpenIndex)
            if (closeParen < 0) break
            index = closeParen + 1
            body.substring(routeOpenIndex + 1, closeParen)
        } else {
            index = routeOpenIndex + 1
            ""
        }
        val positionalPath = parseFirstStringArgument(routeBody)
        val routeFunction = routeMatch.groupValues[1]
        val method = if (routeFunction == "webSocket") "GET" else routeFunction.uppercase()
        val path = values["docPath"].orEmpty().ifBlank { values["path"].orEmpty().ifBlank { positionalPath.orEmpty() } }
        docs += RouteDoc(
            routerClass = routerClass,
            method = method,
            path = path,
            summary = values["summary"].orEmpty(),
            response = schemaType(values["response"].orEmpty(), values["responseList"] == "true"),
            request = schemaType(values["request"].orEmpty(), false),
            query = values["query"].orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() },
            authenticated = values["authenticated"] == "true",
            websocket = routeFunction == "webSocket",
        )
    }
    return docs
}

private fun parseFirstStringArgument(body: String): String? {
    val trimmed = body.trimStart()
    if (!trimmed.startsWith('"')) return null
    val builder = StringBuilder()
    var escaped = false
    for (i in 1 until trimmed.length) {
        val c = trimmed[i]
        if (escaped) {
            builder.append(c)
            escaped = false
        } else if (c == '\\') {
            escaped = true
        } else if (c == '"') {
            return builder.toString()
        } else {
            builder.append(c)
        }
    }
    return null
}

private fun parseNamedArguments(body: String): Map<String, String> {
    val values = mutableMapOf<String, String>()
    val regex = Regex("""([A-Za-z0-9_]+)\s*=\s*("([^"\\]|\\.)*"|[A-Za-z0-9_.]+::class|true|false)""")
    for (match in regex.findAll(body)) {
        val rawValue = match.groupValues[2]
        values[match.groupValues[1]] = when {
            rawValue.startsWith('"') -> rawValue.substring(1, rawValue.length - 1)
            rawValue.endsWith("::class") -> rawValue.removeSuffix("::class").substringAfterLast('.')
            else -> rawValue
        }
    }
    return values
}

private fun schemaType(type: String, list: Boolean): String {
    if (type.isBlank() || type == "Unit") return ""
    return if (list) "List<$type>" else type
}

private fun parseSchemas(sourceRoots: List<File>): SchemaIndex {
    val dataClasses = linkedMapOf<String, SchemaModel>()
    val enums = linkedMapOf<String, EnumModel>()
    sourceRoots
        .filter { it.exists() }
        .flatMap { root -> root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }
        .forEach { file ->
            val text = stripLineComments(file.readText())
            parseDataClasses(text).forEach { dataClasses[it.name] = it }
            parseEnums(text).forEach { enums[it.name] = it }
        }
    return SchemaIndex(dataClasses, enums)
}

private fun parseDataClasses(text: String): List<SchemaModel> {
    val schemas = mutableListOf<SchemaModel>()
    var index = 0
    val regex = Regex("""data\s+class\s+([A-Za-z0-9_]+)\s*\(""")
    while (true) {
        val match = regex.find(text, index) ?: break
        val openParen = match.range.last
        val closeParen = findMatchingParen(text, openParen)
        if (closeParen < 0) break
        val name = match.groupValues[1]
        val properties = splitTopLevel(text.substring(openParen + 1, closeParen), ',')
            .mapNotNull(::parseConstructorProperty)
        schemas += SchemaModel(name, properties)
        index = closeParen + 1
    }
    return schemas
}

private fun parseConstructorProperty(raw: String): SchemaProperty? {
    val body = raw.trim()
    val match = Regex(
        """^(?:@[A-Za-z0-9_.]+(?:\([^)]*\))?\s*)*(?:val|var)\s+([A-Za-z0-9_]+)\s*:\s*([^=]+?)(\s*=\s*.+)?$""",
        RegexOption.DOT_MATCHES_ALL
    )
        .find(body) ?: return null
    val name = match.groupValues[1]
    val type = match.groupValues[2].trim()
    val hasDefault = match.groupValues[3].isNotBlank()
    return SchemaProperty(name, type, required = !hasDefault)
}

private fun parseEnums(text: String): List<EnumModel> {
    val enums = mutableListOf<EnumModel>()
    var index = 0
    val regex = Regex("""enum\s+class\s+([A-Za-z0-9_]+)\s*\{""")
    while (true) {
        val match = regex.find(text, index) ?: break
        val openBrace = match.range.last
        val closeBrace = findMatchingBrace(text, openBrace)
        if (closeBrace < 0) break
        val values = splitTopLevel(text.substring(openBrace + 1, closeBrace), ',')
            .map { it.trim().substringBefore('(').substringBefore(';').trim() }
            .filter { it.matches(Regex("""[A-Z][A-Za-z0-9_]*""")) }
        enums += EnumModel(match.groupValues[1], values)
        index = closeBrace + 1
    }
    return enums
}

private fun splitTopLevel(text: String, delimiter: Char): List<String> {
    val parts = mutableListOf<String>()
    var start = 0
    var parenDepth = 0
    var angleDepth = 0
    var inString = false
    var escaped = false
    for (i in text.indices) {
        val c = text[i]
        if (inString) {
            if (escaped) {
                escaped = false
            } else if (c == '\\') {
                escaped = true
            } else if (c == '"') {
                inString = false
            }
            continue
        }
        when (c) {
            '"' -> inString = true
            '(' -> parenDepth++
            ')' -> parenDepth--
            '<' -> angleDepth++
            '>' -> angleDepth--
            delimiter -> if (parenDepth == 0 && angleDepth == 0) {
                parts += text.substring(start, i)
                start = i + 1
            }
        }
    }
    parts += text.substring(start)
    return parts
}

private fun stripLineComments(text: String): String {
    return text.lines()
        .joinToString("\n") { line -> line.substringBefore("//") }
}

private fun findMatchingParen(text: String, openParen: Int): Int {
    return findMatching(text, openParen, '(', ')')
}

private fun findMatchingBrace(text: String, openBrace: Int): Int {
    return findMatching(text, openBrace, '{', '}')
}

private fun findMatching(text: String, openIndex: Int, openChar: Char, closeChar: Char): Int {
    var depth = 0
    var inString = false
    var escaped = false
    for (i in openIndex until text.length) {
        val c = text[i]
        if (inString) {
            if (escaped) {
                escaped = false
            } else if (c == '\\') {
                escaped = true
            } else if (c == '"') {
                inString = false
            }
            continue
        }
        when (c) {
            '"' -> inString = true
            openChar -> depth++
            closeChar -> {
                depth--
                if (depth == 0) return i
            }
        }
    }
    return -1
}

private fun joinPaths(base: String, child: String): String {
    val joined = listOf(base, child)
        .filter { it.isNotBlank() && it != "/" }
        .joinToString("/") { it.trim('/') }
    return if (joined.isBlank()) "/" else "/$joined"
}

private fun renderOpenApi(routes: List<RouteDoc>, schemaIndex: SchemaIndex): String {
    val components = collectComponentNames(routes, schemaIndex)

    return buildString {
        appendLine("{")
        appendLine("  \"openapi\": \"3.0.3\",")
        appendLine("  \"info\": {")
        appendLine("    \"title\": \"WebCTC API\",")
        appendLine("    \"version\": \"1.1.1\"")
        appendLine("  },")
        appendLine("  \"paths\": {")
        val routeGroups = routes.groupBy { it.path }.entries.sortedBy { it.key }
        routeGroups.forEachIndexed { pathIndex, (path, pathRoutes) ->
            appendLine("    \"${json(path)}\": {")
            pathRoutes.sortedBy { it.method }.forEachIndexed { methodIndex, route ->
                val tag = json(route.routerClass.removeSuffix("Router"))
                val summary = json(route.summary.ifBlank { route.method + " " + route.path })
                appendLine("      \"${route.method.lowercase()}\": {")
                appendLine("        \"summary\": \"$summary\",")
                appendLine("        \"tags\": [\"$tag\"],")
                if (route.authenticated) {
                    appendLine("        \"security\": [{\"cookieAuth\": []}],")
                }
                appendParameters(route)
                if (route.request.isNotBlank()) {
                    appendLine("        \"requestBody\": {")
                    appendLine("          \"content\": {")
                    appendLine("            \"application/json\": {")
                    appendLine("              \"schema\": ${schemaForType(route.request, schemaIndex)}")
                    appendLine("            }")
                    appendLine("          }")
                    appendLine("        },")
                }
                appendLine("        \"responses\": {")
                appendLine("          \"200\": {")
                appendLine("            \"description\": \"OK\",")
                if (route.websocket) {
                    appendLine("            \"content\": {\"application/json\": {\"schema\": {\"type\": \"object\"}}}")
                } else if (route.response.isNotBlank() && route.response != "Unit") {
                    appendLine(
                        "            \"content\": {\"application/json\": {\"schema\": ${
                            schemaForType(
                                route.response,
                                schemaIndex
                            )
                        }}}"
                    )
                } else {
                    appendLine("            \"content\": {}")
                }
                appendLine("          }")
                appendLine("        }")
                append("      }")
                appendLine(if (methodIndex == pathRoutes.lastIndex) "" else ",")
            }
            append("    }")
            appendLine(if (pathIndex == routeGroups.lastIndex) "" else ",")
        }
        appendLine("  },")
        appendLine("  \"components\": {")
        appendLine("    \"securitySchemes\": {")
        appendLine("      \"cookieAuth\": {\"type\": \"apiKey\", \"in\": \"cookie\", \"name\": \"user-session\"}")
        appendLine("    },")
        appendLine("    \"schemas\": {")
        components.forEachIndexed { index, component ->
            append(renderComponentSchema(component, schemaIndex).prependIndent("      "))
            appendLine(if (index == components.lastIndex) "" else ",")
        }
        appendLine("    }")
        appendLine("  }")
        appendLine("}")
    }
}

private fun collectComponentNames(routes: List<RouteDoc>, schemaIndex: SchemaIndex): List<String> {
    val visited = linkedSetOf<String>()
    val queue = ArrayDeque<String>()
    routes.flatMap { listOf(it.request, it.response) }
        .flatMap(::referencedTypeNames)
        .forEach { queue.add(it) }

    while (queue.isNotEmpty()) {
        val name = queue.removeFirst()
        if (!isComponentType(name, schemaIndex) || !visited.add(name)) continue
        schemaIndex.dataClasses[name]?.properties
            ?.flatMap { referencedTypeNames(it.type) }
            ?.forEach { queue.add(it) }
    }
    return visited.sorted()
}

private fun isComponentType(name: String, schemaIndex: SchemaIndex): Boolean {
    return name in schemaIndex.dataClasses || name in schemaIndex.enums
}

private fun renderComponentSchema(name: String, schemaIndex: SchemaIndex): String {
    schemaIndex.enums[name]?.let { enum ->
        return buildString {
            appendLine("\"${json(name)}\": {")
            appendLine("  \"type\": \"string\",")
            appendLine("  \"enum\": [${enum.values.joinToString(", ") { "\"${json(it)}\"" }}]")
            append("}")
        }
    }

    val model = schemaIndex.dataClasses[name]
    if (model == null) {
        return "\"${json(name)}\": {\"type\": \"object\"}"
    }

    return buildString {
        appendLine("\"${json(name)}\": {")
        appendLine("  \"type\": \"object\",")
        if (model.properties.any { it.required }) {
            appendLine(
                "  \"required\": [${
                    model.properties.filter { it.required }.joinToString(", ") { "\"${json(it.name)}\"" }
                }],"
            )
        }
        appendLine("  \"properties\": {")
        model.properties.forEachIndexed { index, property ->
            append("    \"${json(property.name)}\": ${schemaForType(property.type, schemaIndex)}")
            appendLine(if (index == model.properties.lastIndex) "" else ",")
        }
        appendLine("  }")
        append("}")
    }
}

private fun StringBuilder.appendParameters(route: RouteDoc) {
    val params = pathParams(route.path).map { it to "path" } + route.query.map { it to "query" }
    if (params.isEmpty()) return

    appendLine("        \"parameters\": [")
    params.forEachIndexed { index, (name, location) ->
        appendLine("          {")
        appendLine("            \"name\": \"${json(name)}\",")
        appendLine("            \"in\": \"$location\",")
        appendLine("            \"required\": ${location == "path"},")
        appendLine("            \"schema\": {\"type\": \"string\"}")
        append("          }")
        appendLine(if (index == params.lastIndex) "" else ",")
    }
    appendLine("        ],")
}

private fun pathParams(path: String): List<String> {
    return Regex("""\{([^}]+)}""").findAll(path).map { it.groupValues[1] }.toList()
}

private fun schemaRef(type: String): String {
    return if (type.startsWith("List<") && type.endsWith(">")) {
        val elementType = type.removePrefix("List<").removeSuffix(">")
        "{\"type\": \"array\", \"items\": ${schemaRef(elementType)}}"
    } else {
        "{\"\$ref\": \"#/components/schemas/${json(type)}\"}"
    }
}

private val primitiveTypeNames = setOf(
    "String",
    "Char",
    "Boolean",
    "Byte",
    "Short",
    "Int",
    "Long",
    "Float",
    "Double",
    "ByteArray",
    "Unit",
)

private fun schemaForType(rawType: String, schemaIndex: SchemaIndex): String {
    val nullable = rawType.trim().endsWith("?")
    val type = normalizeType(rawType)
    val schema = when {
        type == "String" || type == "Char" -> """{"type": "string"}"""
        type == "Boolean" -> """{"type": "boolean"}"""
        type == "Byte" || type == "Short" || type == "Int" -> """{"type": "integer", "format": "int32"}"""
        type == "Long" -> """{"type": "integer", "format": "int64"}"""
        type == "Float" -> """{"type": "number", "format": "float"}"""
        type == "Double" -> """{"type": "number", "format": "double"}"""
        type == "ByteArray" -> """{"type": "string", "format": "byte"}"""
        type == "Uuid" || type == "UUID" -> """{"type": "string", "format": "uuid"}"""
        type.startsWith("List<") && type.endsWith(">") -> {
            val elementType = type.removePrefix("List<").removeSuffix(">")
            """{"type": "array", "items": ${schemaForType(elementType, schemaIndex)}}"""
        }

        type.startsWith("Set<") && type.endsWith(">") -> {
            val elementType = type.removePrefix("Set<").removeSuffix(">")
            """{"type": "array", "uniqueItems": true, "items": ${schemaForType(elementType, schemaIndex)}}"""
        }

        type.startsWith("Map<") && type.endsWith(">") -> {
            val valueType =
                splitTopLevel(type.removePrefix("Map<").removeSuffix(">"), ',').getOrNull(1)?.trim().orEmpty()
            """{"type": "object", "additionalProperties": ${schemaForType(valueType.ifBlank { "Any" }, schemaIndex)}}"""
        }

        type == "Any" || type.endsWith("IShape") || type.endsWith("IRange") || type.endsWith("IRailMapData") -> """{"type": "object"}"""
        type in schemaIndex.enums || type in schemaIndex.dataClasses -> """{"${"$"}ref": "#/components/schemas/${
            json(
                type
            )
        }"}"""

        else -> """{"type": "object"}"""
    }
    return if (nullable) addNullable(schema) else schema
}

private fun normalizeType(rawType: String): String {
    return rawType.trim()
        .removeSuffix("?")
        .substringAfterLast('.')
        .replace(Regex("""\s+"""), "")
}

private fun referencedTypeNames(rawType: String): List<String> {
    val type = normalizeType(rawType)
    if (type.isBlank()) return emptyList()
    if (type.startsWith("List<") && type.endsWith(">")) {
        return referencedTypeNames(type.removePrefix("List<").removeSuffix(">"))
    }
    if (type.startsWith("Set<") && type.endsWith(">")) {
        return referencedTypeNames(type.removePrefix("Set<").removeSuffix(">"))
    }
    if (type.startsWith("Map<") && type.endsWith(">")) {
        return splitTopLevel(type.removePrefix("Map<").removeSuffix(">"), ',').drop(1).flatMap(::referencedTypeNames)
    }
    return listOf(type)
}

private fun addNullable(schema: String): String {
    return schema.dropLast(1) + """, "nullable": true}"""
}

private fun json(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}
