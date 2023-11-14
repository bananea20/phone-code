package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcore.AISDK
import kotlin.math.floor
import kotlin.math.max

class ListBoxPlotFilter(enable: Boolean = false, debug: Boolean = true) : AbsListFilter() {
    companion object {
        private const val TAG = "ListBoxPlotFilter"
    }

    private val debug: Boolean
    private val enable: Boolean

    init {
        this.enable = enable
        this.debug = debug
        AISDK.logger()?.i(TAG, "enable ${this.enable}")
    }

    private fun filterText(list: List<String>): List<Boolean> {
        var charCounter: MutableList<MutableSet<Char>> = mutableListOf()
        for (str in list) {
            str.forEachIndexed { i, c ->
                run {
                    if (i >= charCounter.size) {
                        charCounter.add(mutableSetOf())
                    }
                    charCounter[i].add(c)
                }
            }
        }
        var dim: Int = 0
        var map: MutableList<MutableMap<Char, Int>> = mutableListOf()
        for (s in charCounter) {
            dim = max(dim, s.size)
            map.add(mutableMapOf())
            val t = s.sortedBy { c -> c }
            t.forEachIndexed { i, c -> map.last()[c] = i + 1 }
        }
        dim++
        if (debug) {
            AISDK.logger()?.i("BoxPlot", "dim: $dim")
            map.forEachIndexed { i, m ->
                run {
                    for ((a, b) in m) {
                        AISDK.logger()?.i("BoxPlot", "$i $a $b")
                    }
                }
            }
        }
        var numbs: MutableList<Long> = mutableListOf()
        for (str in list) {
            numbs.add(textToNumber(str, map, dim))
        }
        val res = filterNumber(numbs)
        list.forEachIndexed { i, s ->
            run {
                if (debug) {
                    AISDK.logger()?.i(TAG, "$s -> ${numbs[i]} -> ${res[i]}")
                }
            }
        }
        return res
    }

    private fun textToNumber(text: String, map: List<Map<Char, Int>>, dim: Int): Long {
        var res: Long = 0
        text.forEachIndexed { i, c ->
            run {
                res = res * dim + map[i][c]!!
            }
        }
        return res
    }

    private fun filterNumber(numbs: List<Long>): List<Boolean> {
        var res: MutableList<Boolean> = mutableListOf()
        if (numbs.size < 4) {
            for (e in numbs) {
                res.add(true)
            }
            return res
        }
        val q1 = quartile(numbs, 0.25)
        val q3 = quartile(numbs, 0.75)
        val iqr = q3 - q1
        for (n in numbs) {
            if (q1 - 1.5 * iqr <= n && n <= q3 + 1.5 * iqr) {
                res.add(true)
            } else {
                res.add(false)
            }
        }
        return res
    }

    private fun quartile(list: List<Long>, q: Double): Double {
        val numbs = list.sorted()
        val n = numbs.size
        val k = floor(q * (n + 1)).toInt()
        val alpha = q * (n + 1) - floor(q * (n + 1))
        return numbs[k - 1] + alpha * (numbs[k] - numbs[k - 1])
    }

    override operator fun invoke(list: List<TextInfo>): List<TextInfo> {
        if (!enable) return list
        var lst: MutableList<String> = mutableListOf()
        for (e in list) {
            lst.add(e.t)
        }
        val selection = filterText(lst)
        var tmpList: MutableList<TextInfo> = mutableListOf()
        list.forEachIndexed { i, e ->
            run {
                if (selection[i]) {
                    tmpList.add(e)
                } else {
                    AISDK.logger()?.i(TAG, "${e.t} filtered by boxplot")
                }
            }
        }
        return tmpList
    }
}