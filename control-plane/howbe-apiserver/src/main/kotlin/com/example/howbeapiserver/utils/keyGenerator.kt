package com.example.howbeapiserver.utils

fun createKey(objectType: String?, name: String?): String {
    val parts = mutableListOf<String>()
    parts.add("registry")

    if (!objectType.isNullOrBlank()) {
        parts.add(objectType.lowercase())
    }

    if (!name.isNullOrBlank()) {
        parts.add(name)
    }

    return "/" + parts.joinToString("/")
}

fun getNextKey(key: String): String {
    val lastChar = key.last()
    return if (lastChar < Char.MAX_VALUE) {
        key.dropLast(1) + (lastChar + 1)
    } else {
        key + Char.MIN_VALUE
    }
}