package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcore.AISDK
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.sqrt

class TextSizeFilter() : AbsTextFilter() {
    companion object {
        private const val TAG = "TextSizeFilter"
    }

    private val textScale = 1.0f
    private val minTextWidth = 83.35f
    private val minTextHeight = 21.35f
    private val minTextArea = 2067.85f

    private fun distOfPoints(a: Point, b: Point): Double {
        val x = (a.x - b.x) * (a.x - b.x)
        val y = (a.y - b.y) * (a.y - b.y)
        return sqrt(x + y)
    }

    private fun getMinAreaSize(points: List<Point>): List<Double> {
        var pnt = MatOfPoint2f()
        pnt.fromList(points)
        val boundingBox = Imgproc.minAreaRect(pnt)
        val lst0 = MatOfPoint2f()
        Imgproc.boxPoints(boundingBox, lst0)
        var lst1 = lst0.toList()
        lst1.sortedWith(compareBy<Point> { it.x })
        var indexa = 0
        var indexb = 1
        var indexc = 2
        var indexd = 3
        if (lst1[1].y > lst1[0].y) {
            indexa = 0
            indexd = 1
        } else {
            indexa = 1
            indexd = 0
        }
        if (lst1[3].y > lst1[2].y) {
            indexb = 2
            indexc = 3
        } else {
            indexb = 3
            indexc = 2
        }
        val box = listOf(lst1[indexa], lst1[indexb], lst1[indexc], lst1[indexd])
        var w = max(distOfPoints(box[0], box[1]), distOfPoints(box[2], box[3]))
        var h = max(distOfPoints(box[0], box[3]), distOfPoints(box[1], box[2]))
        if (h > w) {
            val t = w
            w = h
            h = t
        }
        return listOf(w, h, w * h)
    }

    override operator fun invoke(text: String, info: List<InnerOCR>): List<InnerOCR> {
        var sls: MutableList<InnerOCR> = mutableListOf()
        for (e in info) {
            var pnt: MutableList<Point> = mutableListOf()
            for (p in e.text.box) {
                pnt.add(Point(p.x.toDouble(), p.y.toDouble()))
            }
            val s = getMinAreaSize(pnt)
            if (s[0] < textScale * minTextWidth || s[1] < textScale * minTextHeight || s[2] < textScale * minTextArea) {
                AISDK.logger()?.i(
                    TAG,
                    "$text filtered by text size ${s[0]} ${s[1]} ${s[2]}(w,h,arwa), ${textScale * minTextWidth} ${textScale * minTextHeight} ${textScale * minTextArea}."
                )
                continue
            }
            sls.add(e)
        }
        if (sls.size < info.size) {
            AISDK.logger()?.i(
                TAG,
                "$text filtered by text size, size decreased from ${info.size} to ${sls.size}."
            )
        }
        return sls
    }
}