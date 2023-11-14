package com.skyui.aiengine.libcarport.module

abstract class AbsTextFilter {
    open operator fun invoke(text: String, info: List<InnerOCR>): List<InnerOCR> {
        return emptyList()
    }
}