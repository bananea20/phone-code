package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcore.AISDK

class TextConfidenceFilter() : AbsTextFilter() {
    companion object {
        private const val TAG = "TextConfidenceFilter"
    }

    private val minConfidence = 0.5f
    override operator fun invoke(text: String, info: List<InnerOCR>): List<InnerOCR> {
        val fls = info.filter { o -> o.text.confidence >= minConfidence }
        if (fls.size < info.size) {
            AISDK.logger()?.i(
                TAG,
                "$text filtered by threshold ${minConfidence}, size decreased from ${info.size} to ${fls.size}."
            )
        }
        return fls
    }

}