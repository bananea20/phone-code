package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcarport.bean.ParkingResult
import com.skyui.aiengine.libcore.AISDK
import kotlin.math.min

data class InnerOCR(val text: Text, val stamp: Double, val camera: String, val path: String)
data class PostProcessInput(val data: Map<String, List<InnerOCR>>, val map: HashMap<String, String>)
data class PostProcessOutput(val data: ParkingResult)
data class TextInfo(val t: String, val n: Int, val s: Double, val d: Double, val m: Double)

// 后处理

class PostProcessor {

    companion object {
        private const val TAG = "PostProcessor" // 
        private const val findMaster = false
        private val textFilters: List<AbsTextFilter> =   // 变量
            listOf(TextRegexFilter(), TextConfidenceFilter(), TextSizeFilter(), TextWindowFilter(0))
        private val listFilters: List<AbsListFilter> =
            listOf(ListBoxPlotFilter(enable = false), ListMergeFilter(enable = true))
    }

    operator fun invoke(input: PostProcessInput): PostProcessOutput {
        var flist: MutableList<AbsTextFilter> = mutableListOf(TextMapFilter(input.map))
        flist.addAll(textFilters)
        var list: MutableList<TextInfo> = mutableListOf()
        for ((t, ls) in input.data) {
            var tst: List<InnerOCR> = ls
            for (f in flist) {
                tst = f(t, tst)
                if (tst.isEmpty()) break
            }
            val n = tst.size
            if (n <= 0) continue
            val s = tst.sumOf { it.text.confidence.toDouble() } / n // 平均置信度
            val d = tst.sumOf { it.stamp } / n
            val m = tst.sumOf { if (it.camera in setOf("BACK", "FRONT")) 1.0 else 0.0 } / n
            list.add(TextInfo(t, n, s, d, m))
        }
        var xst: List<TextInfo> = list
        for (f in listFilters) {
            xst = f(xst)
            if (xst.isEmpty()) break
        }
        var lst: MutableList<TextInfo> = xst.toMutableList()
        var r = ParkingResult()
        lst.sortWith(compareBy<TextInfo> { it.n }.thenBy { it.s }.thenBy { it.d }.thenBy { it.m })
        lst.reverse()
        for (e in lst) {
            AISDK.logger()?.i(
                "CarportCandidate", "${e.t} ${e.n} ${"%.4f".format(e.s)} " +
                        "${"%.4f".format(e.d)} ${"%.4f".format(e.m)}"
            )
        }
        if (findMaster) {
            for (i in 0 until min(3, lst.size)) {
                if (lst[i].m < 0.8 || lst[i].n < 2 || lst[i].s < 0.9) {
                    continue
                }
                r.masterId = lst[i].t
                r.masterConf = lst[i].s.toFloat()
                break
            }
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
        var floor2lst: HashMap<String, MutableList<Double>> = hashMapOf()
        if (input.map.isEmpty()) {
            r.message += "map is empty, can not get floor id. "
        }
        for (e in lst) {
            if (!input.map.containsKey(e.t)) {
                continue
            }
            val f = input.map[e.t]!!
            val s = e.s
            if (!floor2lst.containsKey((f))) {
                floor2lst[f] = mutableListOf()
            }
            floor2lst[f]?.add(s)
        }
        val fs = floor2lst.toList().sortedByDescending { (_, v) -> v.size }
        if (fs.isNotEmpty()) {
            r.floorId = fs[0].first
            r.floorConf = fs[0].second.average().toFloat()
        }
        return PostProcessOutput(r)
    }
}