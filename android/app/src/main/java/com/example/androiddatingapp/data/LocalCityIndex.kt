package com.example.androiddatingapp.data

import android.content.Context
import java.text.Normalizer
import java.util.Locale

object LocalCityIndex {

    @Volatile
    private var cityNames: List<String>? = null

    fun search(context: Context, query: String, limit: Int = 10): List<String> {
        val names = load(context)
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return emptyList()
        return names
            .asSequence()
            .filter { normalize(it).startsWith(normalizedQuery) }
            .distinct()
            .take(limit)
            .toList()
    }

    private fun load(context: Context): List<String> {
        cityNames?.let { return it }
        val loaded = context.applicationContext.assets
            .open("city_names.txt")
            .bufferedReader()
            .useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
        cityNames = loaded
        return loaded
    }

    private fun normalize(value: String): String {
        val lower = value.trim().lowercase(Locale.getDefault())
        val decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD)
        return decomposed.replace("\\p{Mn}+".toRegex(), "")
    }
}
