package com.skyui.aiengine.libcarport.module

abstract class AbsListFilter {
    open operator fun invoke(list:List<TextInfo>): List<TextInfo> {
        return emptyList()
    }
}