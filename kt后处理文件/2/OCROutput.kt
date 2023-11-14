package com.skyui.aiengine.libcarport.module

data class Point(var x: Float = -1f, var y: Float = -1f)
data class Text(
    var box: List<Point> = emptyList(), var width: Int = 0, var height: Int = 0,
    var value: String = "", var confidence: Float = 0f, var direction: String = ""
)

data class OCROutput(
    var data: List<List<Text>> = emptyList()
)