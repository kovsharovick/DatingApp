package com.example.androiddatingapp.data

import java.text.Normalizer

object CityStrings {
    fun normalize(value: String): String =
        Normalizer.normalize(
            value.trim()
                .replace("\uFEFF", "")
                .replace('\u00A0', ' ')
                .replace(Regex("\\s+"), " "),
            Normalizer.Form.NFC,
        )
}
