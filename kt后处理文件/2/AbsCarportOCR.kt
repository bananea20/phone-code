package com.skyui.aiengine.libcarport.module

import android.graphics.Bitmap
import android.graphics.Matrix

abstract class AbsCarportOCR(isRotate: Boolean = false) {
    var isRotate: Boolean = false
    open fun init() {}
    open fun rotateBitmap(src: Bitmap): List<Bitmap> {
        var res: List<Bitmap> = listOf()
        for (a in listOf(0f, 90f, 180f, 270f)) {
            res += if (a > 0) {
                val matrix = Matrix()
                matrix.postRotate(a)
                Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
            } else {
                src
            }
        }
        return res
    }

    open suspend fun invoke(images: List<Bitmap>): OCROutput {
        return OCROutput()
    }

    init {
        this.isRotate = isRotate
        init()
    }
}