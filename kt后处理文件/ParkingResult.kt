package com.skyui.aiengine.libcarport.bean

/**
 * @author : create by owen.ou
 * 版本 1.0
 * 创建日期: 2022/11/8
 * 描述: 车位识别结果
 */
data class ParkingResult(
    var status: Int = 0,
    var message: String = "",
    var masterId: String = "",
    var masterConf: Float = 0f, // 识别的情况定阈值，置信度内置，不用输出置信度
    var neighborId: String = "",
    var neighborConf: Float = 0f,
    var floorId: String = "",
    var floorConf: Float = 0f,
    var vid: String = "",
    var stamp: String = "",
    var debug: String = "",
    var info: CarData = CarData()
)