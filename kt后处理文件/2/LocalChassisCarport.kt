package com.skyui.aiengine.libcarport.module

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.skyui.aiengine.libcarport.bean.DVRImage
import com.skyui.aiengine.libcarport.bean.ParkingResult
import com.skyui.aiengine.libcarport.callback.CarportRecCallback
import com.skyui.aiengine.libcore.AISDK
import com.skyui.aiengine.libcore.APPScope
import kotlinx.coroutines.launch
import kotlin.math.min

class LocalChassisCarport : ICarportRec() {
    companion object {
        private const val TAG = "LocalChassisCarport"
        private const val chassisThreshold = 0.5f
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
        APPScope.launch {
            process(list, map).also {
                it.vid = vid
                it.stamp = stamp
                callback.onRecognize(it)
            }
        }
    }

    private suspend fun process(list: List<DVRImage>, map: HashMap<String, String>): ParkingResult {
        var imgs = listOf<Bitmap?>()
        for (e in list.sortedByDescending { it.stamp }) {
            try {
                val bitmap = BitmapFactory.decodeFile(e.chassis)
                imgs += bitmap
            } catch (err: Exception) {
                AISDK.logger()?.i(TAG, "load bitmap from ${e.chassis} failed: ${err.message}.")
                imgs += null
            }
        }
        var txt2lst: HashMap<String, List<Triple<Text, Int, Int>>> = hashMapOf()
        for (i in list.indices) {
            if (imgs[i] == null) continue
            val ims = listOf(imgs[i]!!)
            try {
                val res = ocr.invoke(ims)
                for (j in ims.indices) {
                    for (e in res.data[j]) {
                        // ([[0,0], [0, 1], [1, 1], [1, 0], [w, h]], 'B12', 0.98)
                        val t = e.value.trim()
                        if (t.isEmpty()) {
                            continue
                        }
                        if (!isCorrectFormat((t))) {
                            AISDK.logger()?.i(TAG, "$t filtered by format.")
                            continue
                        }
                        if (map.isNotEmpty() and !map.containsKey(t)) {
                            AISDK.logger()?.i(TAG, "$t filtered by map.")
                            continue
                        }
                        if (e.confidence < chassisThreshold) {
                            AISDK.logger()?.i(TAG, "$t filtered by threshold ${chassisThreshold}.")
                            continue
                        }
                        val x = Triple(e, i, j)
                        if (!txt2lst.containsKey(t)) {
                            txt2lst[t] = emptyList()
                        }
                        txt2lst[t] = txt2lst[t]?.plus(x)!!
                    }
                }
            } catch (err: Exception) {
                AISDK.logger()?.i(TAG, "ocr recognition failed: ${err.message}.")
            }
        }
        data class TextInfo(val t: String, val n: Int, val s: Double, val d: Double, val m: Double)

        var lst: List<TextInfo> = listOf()
        var textList: List<String> = emptyList()
        for ((t, ls) in txt2lst) {
            val n = ls.size
            val s = ls.sumOf { it.first.confidence.toDouble() } / n // 平均置信度
            val d = ls.sumOf { it.second.toDouble() } / n
            val m = ls.sumOf { if (isMaster(it.first)) 1.0 else 0.0 } / n
            lst += TextInfo(t, n, s, d, m)
            textList = textList.plusElement(t)
            AISDK.logger()?.i(
                TAG,
                "$t $n ${"%.4f".format(s)} ${"%.4f".format(d)} ${"%.4f".format(m)}"
            )
        }
        var r = ParkingResult()
        lst.sortedWith(compareBy<TextInfo> { it.n }.thenBy { it.s }.thenBy { -it.d }
            .thenBy { it.m })
        lst.reversed()
        for (i in 0 until min(3, lst.size)) {
            if (lst[i].m < 0.5) {
                continue
            }
            r.masterId = lst[i].t
            r.masterConf = lst[i].s.toFloat()
            break
        }
        if (r.masterId.isEmpty()) {
            if (lst.isNotEmpty()) {
                r.masterId = lst[0].t
                r.masterConf = lst[0].s.toFloat()
            }
            if (lst.size > 1) {
                r.neighborId = lst[1].t
                r.neighborConf = lst[1].s.toFloat()
            }
        }
        var floor2lst: HashMap<String, List<Double>> = hashMapOf()
        if (map.isEmpty()) {
            r.message += "map is empty, can not get floor id. "
        }
        for (e in lst) {
            if (!map.containsKey(e.t)) {
                continue
            }
            val f = map[e.t]!!
            val s = e.s
            if (!floor2lst.containsKey((f))) {
                floor2lst[f] = emptyList()
            }
            floor2lst[f]?.plus(s)
        }
        val fs = floor2lst.toList().sortedByDescending { (_, v) -> v.size }
        if (fs.isNotEmpty()) {
            r.floorId = fs[0].first
            r.floorConf = fs[0].second.average().toFloat()
        }
        return r
    }

    private fun isCorrectFormat(str: String): Boolean {
        val pat = Regex("^临?[ -]?([a-zA-Z]\\d*)?(-|( )+)?([a-zA-Z\\d]+[ -]?)?\\d+")
        return pat.matchEntire(str) != null
    }

    private fun isMaster(txt: Text): Boolean {
        val w = txt.width.toDouble()
        val x = txt.box.sumOf { it.x.toDouble() } / 4
        val b = w / 3
        return b <= x && x <= w - b
    }
}