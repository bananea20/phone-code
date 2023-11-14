package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcore.AISDK


// 和json进行map
class TextMapFilter(map: HashMap<String, String>) : AbsTextFilter() {
    companion object {
        private const val TAG = "TextMapFilter"
    }

    var map: HashMap<String, String>
    override operator fun invoke(text: String, info: List<InnerOCR>): List<InnerOCR> {
        if (map.isNotEmpty() and !map.containsKey(text)) {
            AISDK.logger()?.i(TAG, "$text filtered by map.")
            return emptyList()
        }
        return info
    }

    init {
        this.map = map
    }
}