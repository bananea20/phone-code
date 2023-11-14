package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcarport.bean.DVRImage
import com.skyui.aiengine.libcarport.callback.CarportRecCallback

/**
 * @author : create by owen.ou
 * 版本 1.0
 * 创建日期: 2022/11/23
 * 描述: 车位识别接口
 */
abstract class ICarportRec {

    abstract suspend fun carportRec(
        list: List<DVRImage>,
        map: HashMap<String, String>,
        callback: CarportRecCallback,
        vid: String = "",
        stamp: String = ""
    )

    suspend fun carportRec(
        tarPath: String,
        map: HashMap<String, String>,
        callback: CarportRecCallback
    ) {
        val d = PreProcessor().process(tarPath)
        carportRec(d.images, map, callback, d.vid)
    }
}