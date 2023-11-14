package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcarport.bean.DVRImage
import com.skyui.aiengine.libcarport.callback.CarportRecCallback

/**
 * @author : create by owen.ou
 * 版本 1.0
 * 创建日期: 2022/11/23
 * 描述: 车位识别功能实现类
 */
class CarportRecImpl private constructor() : ICarportRec() {
    companion object {
        private const val version = 0
        val instance: CarportRecImpl by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CarportRecImpl() }
    }

    var imp: ICarportRec = when (version) {
        0 -> LocalDvrCarport()
        1 -> RemoteCarport()
        2 -> LocalChassisCarport()
        else -> LocalDvrCarport()
    }

    init {
        CarportNativeController.instance
    }

    override suspend fun carportRec(
        list: List<DVRImage>,
        map: HashMap<String, String>,
        callback: CarportRecCallback,
        vid: String,
        stamp: String
    ) {
        imp.carportRec(list, map, callback, vid)
    }

}