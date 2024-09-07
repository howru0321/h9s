package com.example.howbeapiserver.grpcstub.watch.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonElement

data class FieldSelectorResult(
    val fieldPath: List<String>,
    val value: String?
)

fun parseFieldSelector(fieldSelector: String): FieldSelectorResult {
    // Split the input string by '=' but limit to 2 parts
    // This ensures we don't split the value if it contains '='
    val parts = fieldSelector.split("=", limit = 2)

    if (parts.size != 2) {
        throw IllegalArgumentException("Invalid field selector format: $fieldSelector")
    }

    val (fieldPath, value) = parts

    // Split the field path by '.' and trim each part
    val fieldList = fieldPath.split(".").map { it.trim() }

    // If value is empty, return null for the value
    val trimmedValue = value.trim().takeIf { it.isNotEmpty() }

    return FieldSelectorResult(fieldList, trimmedValue)
}


fun matchesFieldSelector(jsonString: String, fieldSelector: FieldSelectorResult?): Boolean {
    if(fieldSelector==null){
        return true
    }
    val gson = Gson()
    val outerJsonElement = JsonParser.parseString(jsonString)

    // Extract the "object" field
    val objectJsonElement = if (outerJsonElement is JsonObject && outerJsonElement.has("object")) {
        outerJsonElement.get("object")
    } else {
        return false // If there's no "object" field, it doesn't match
    }

    // Convert the "object" JsonElement back to a JSON string
    val objectJsonString = gson.toJson(objectJsonElement)


    // Now parse the object JSON string
    val jsonElement = JsonParser.parseString(objectJsonString)

    // Traverse the JSON structure based on the field path
    val fieldValue = fieldSelector.fieldPath.fold(jsonElement as JsonElement?) { acc, field ->
        when {
            acc is JsonObject && acc.has(field) -> acc.get(field)
            acc is JsonObject -> null
            else -> null
        }
    }

    // If fieldValue is null, it means the field doesn't exist in the JSON
    if (fieldValue == null) {
        // If the selector value is also null, this is a match (field doesn't exist)
        return fieldSelector.value == null
    }

    // Compare the found value with the selector value
    return when {
        fieldSelector.value == null -> false // Field exists but selector expects it not to
        fieldValue.isJsonPrimitive -> fieldValue.asString == fieldSelector.value
        else -> false // Non-primitive values are not supported for comparison
    }
}