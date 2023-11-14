package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcore.AISDK

class ListMergeFilter(enable: Boolean = true) : AbsListFilter() {
    var enable: Boolean

    companion object {
        private const val TAG = "ListMergeFilter"
    }

    init {
        this.enable = enable
        AISDK.logger()?.i(TAG, "enable ${this.enable}")
    }

    override operator fun invoke(list: List<TextInfo>): List<TextInfo> {
        if (!enable) return list
        var lst = list.toMutableList()
        lst.sortWith(compareBy<TextInfo> { it.t.length }.thenBy { -it.s })
        var res: MutableList<TextInfo> = mutableListOf()
        for (i in 0 until lst.size) {
            var flag = true
            for (j in i + 1 until lst.size) {
                if (lst[i].t in lst[j].t) {
                    flag = false
                    AISDK.logger()?.i(TAG, "merge ${lst[i].t} into ${lst[j].t}")
                    lst[j] = TextInfo(lst[j].t, lst[j].n + lst[i].n, lst[j].s, lst[j].d, lst[j].m)
                    break
                }
            }
            if (flag) {
                res.add(lst[i])
            }
        }
        return res
    }
}