package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcore.AISDK

class TextRegexFilter : AbsTextFilter() {

    companion object {
        private const val TAG = "TextRegexFilter"
    }

    private val regexList: List<Pair<Regex, List<String>>> = listOf(
        Pair(Regex("^\\d{2,4}"), listOf("123")),
        Pair(Regex("^[A-Z]\\d{1,2}(-{1,3}| {1,4})\\d{2,4}"), listOf("B2-1236")),
        Pair(Regex("^[A-Z]\\d{1,2}(-{1,3}| {1,4})[A-Z]\\d{2,4}"), listOf("B3-F198")),
        Pair(Regex("^[A-Z]{1,2}(-{0,3}| {0,4})\\d{2,4}"), listOf("PG-1014")),
        Pair(Regex("^[A-Z]\\d{2,4}"), listOf("B123")),
        Pair(
            Regex("^[A-Z]\\d{1,2}(-{1,3}| {1,4})\\d{1,2}[A-Z](-{1,3}| {1,4})\\d{2,4}"),
            listOf("P6-2F-5164")
        ),
        Pair(
            Regex("^[A-Z]\\d{1,2}(-{1,3}| {1,4})\\d{1,2}(-{1,3}| {1,4})\\d{2,4}"),
            listOf("P9-11-241")
        ),
        Pair(Regex("^[A-Z](-{1,3}| {1,4})\\d{1,4}(-{1,3}| {1,4})\\d{1,4}"), listOf("B 80 1")),
        Pair(Regex("^\\d{2,4}(-{1,3}| {1,4})[a-z]"), listOf("634-a")),
        Pair(Regex("^[A-Z]\\d{1,2}(-{0,3}| {0,4})[A-Z]\\d{2,4}"), listOf("B3A047", "P2E342")),
        Pair(Regex("^[A-Z]{1,2}\\d{1,2}(-{0,3}| {0,4})[A-Z]\\d{2,4}"), listOf("CP2-F089")),
        Pair(Regex("^[A-Z]\\d{1,2}(-{0,3}| {0,4})临(-{0,3}| {0,4})\\d{1,4}"), listOf("B2-临15")),
        Pair(Regex("^临(-{0,3}| {0,4})\\d{2,4}"), listOf("临 123")),
        Pair(
            Regex("^临(-{0,3}| {0,4})[A-Z]\\d{1,2}(-{1,3}| {1,4})\\d{2,4}"),
            listOf("临 B2-123")
        ),
        Pair(Regex("^临(-{0,3}| {0,4})[A-Z]\\d{2,4}"), listOf("临 B123")),
        Pair(
            Regex("^[A-Z]\\d{1,2}(-{1,3}| {1,4})[A-Z](-{1,3}| {1,4})\\d{2,4}"),
            listOf("B2 B-041")
        ),
    )

    private fun isCorrectFormat(str: String): Boolean {
        for (p in regexList) {
            if (p.first.matchEntire(str) != null) {
                return true;
            }
        }
        return false
    }

    override operator fun invoke(text: String, info: List<InnerOCR>): List<InnerOCR> {
        if (!isCorrectFormat(text)) {
            AISDK.logger()?.i(TAG, "$text filtered by format.")
            return emptyList()
        }
        return info
    }

    init {
        for (e in regexList) {
            for (s in e.second) {
                assert(e.first.matchEntire(s) != null) { "number '$s' not match pattern '${e.first}'" }
            }
        }
    }
}