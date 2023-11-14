package com.skyui.aiengine.libcarport.module

import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.skyui.aiengine.libcarport.bean.DVRImage
import com.skyui.aiengine.libcarport.bean.ParkingResult
import com.skyui.aiengine.libcarport.callback.CarportRecCallback
import com.skyui.aiengine.libcore.AISDK

class LocalDvrCarport : ICarportRec() {
    companion object {
        private const val TAG = "LocalDvrCarport"
        private const val ocrVersion = 200
    }

    var ocr: AbsCarportOCR = when (ocrVersion) {
        100 -> ThirdPartyOCR(false)
        200 -> ParkingLotOCR(false)
        else -> throw RuntimeException("unimplemented ocr version $ocrVersion")
    }

    override suspend fun carportRec(
        list: List<DVRImage>,
        map: HashMap<String, String>,
        callback: CarportRecCallback,
        vid: String,
        stamp: String
    ) {
        process(list, map).also {
            it.vid = vid
            it.stamp = stamp
            callback.onRecognize(it)
        }
    }

    private suspend fun process(list: List<DVRImage>, map: HashMap<String, String>): ParkingResult {
        val tic0 = System.currentTimeMillis()
        var txt2lst: HashMap<String, MutableList<InnerOCR>> = hashMapOf()
        var imgIndex = 0
        val tic1 = System.currentTimeMillis()
        var ocrInfo: MutableMap<String, OCROutput> = mutableMapOf()
        for (dvr in list.sortedByDescending { it.stamp }) {
            imgIndex += 1
            for (cam in listOf("BACK", "FRONT", "LEFT", "RIGHT")) {
                val imgPath = when (cam) {
                    "BACK" -> dvr.back
                    "FRONT" -> dvr.front
                    "LEFT" -> dvr.left
                    else -> dvr.right
                }
                try {
                    if (imgPath.isEmpty()) {
                        AISDK.logger()?.i(TAG, "empty image path")
                        continue
                    }
                    val bitmap = BitmapFactory.decodeFile(imgPath)
                    if (bitmap == null) {
                        AISDK.logger()?.i(TAG, "decode image failed from $imgPath")
                        continue
                    }
                    val ims = listOf(bitmap)
                    val res = ocr.invoke(ims)
                    ocrInfo[imgPath] = res
                    for (j in ims.indices) {
                        for (e in res.data[j]) {
                            // ([[0,0], [0, 1], [1, 1], [1, 0], [w, h]], 'B12', 0.98)
                            val t = e.value.trim()
                            if (t.isEmpty()) {
                                continue
                            }
                            val x = InnerOCR(e, dvr.stamp, cam, imgPath)
                            if (!txt2lst.containsKey(t)) {
                                txt2lst[t] = mutableListOf()
                            }
                            txt2lst[t]?.add(x)
                        }
                    }
                } catch (err: Exception) {
                    AISDK.logger()?.e(TAG, "bitmap ocr from $imgPath failed: ${err.message}.", err)
                }
            }
            AISDK.logger()?.i(TAG, "current index: ${imgIndex}/${list.size}")
        }
        val toc1 = System.currentTimeMillis()
        val pp = PostProcessor()
        var result = pp(PostProcessInput(txt2lst, map))
        if (AISDK.isTest()) {
            var gs = Gson()
            result.data.debug = gs.toJson(ocrInfo)
        }
        val toc0 = System.currentTimeMillis()
        AISDK.logger()?.i(
            TAG, "total=${"%.2f".format((toc0 - tic0) / 1000.0)}s, " +
                    "ocr=${"%.2f".format((toc1 - tic1) / 1000.0)}s"
        )
        return result.data
    }
}