package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcarport.bean.DVRImage
import com.skyui.aiengine.libcarport.bean.ParkingResult
import com.skyui.aiengine.libcarport.callback.CarportRecCallback
import com.skyui.aiengine.libcore.AISDK
import com.skyui.aiengine.libcore.APPScope
import com.skyui.aiengine.libcore.network.INetworkAgent
import com.skyui.aiengine.libutils.HttpUtil
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Base64

class RemoteCarport : ICarportRec() {

    companion object {
        private const val TAG = "RemoteCarport"
        private const val app_id = "101260"
        private const val app_secret = "E76052027622Bdccd3C6daa367ec90e1"
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

    private fun buildBody(list: List<DVRImage>, map: HashMap<String, String>): String {
        val imgPath = list[0].chassis
        val encoder: Base64.Encoder = Base64.getEncoder()
        val imgB64: String = encoder.encodeToString(File(imgPath).readBytes())
        val jo = JSONObject()
        val imgObj: JSONObject = JSONObject().also {
            it.put("image", "$imgB64")
        }
        val chassisArray = JSONArray().also {
            it.put(imgObj)
        }
        jo.put("chassis", chassisArray)
        val mapObj: JSONObject = JSONObject().apply {
            map.forEach {
                this.put(it.key, it.value)
            }
        }
        jo.put("map", mapObj)
        return jo.toString()
    }

    private fun process(list: List<DVRImage>, map: HashMap<String, String>): ParkingResult {
        if (list.isEmpty() or list[0].chassis.isEmpty()) {
            return ParkingResult(
                status = -1,
                message = "empty images"
            )
        }

        val postJsonStr = buildBody(list, map)
        val svrUrl = "https://tsp-stg.nio.com/api/1/iot/ai/parking_lot_number"
        val request = INetworkAgent.HttpRequest(
            svrUrl,
            isPost = true,
            json = postJsonStr,
            path = "api/1/iot/ai/parking_lot_number"
        )
            .addPostUrlParam("app_id", app_id)
            .addPostUrlParam("timestamp", System.currentTimeMillis().toString())
        val sign = HttpUtil.calStrMd5(
            HttpUtil.getPreSignStringForPost(
                request,
                app_secret
            )
        )
        request.addPostUrlParam("sign", sign)

        val responseResult = AISDK.networkAgent().request(request, ServerResponse::class.java)
        if (responseResult is INetworkAgent.ResponseResult.Success) {
            responseResult.data?.let { response ->
                AISDK.logger()?.i(TAG, "res = $response")
                return ParkingResult(
                    status = response.data.code,
                    message = response.data.message,
                    masterId = response.data.numbers.master.id,
                    masterConf = response.data.numbers.master.confidence,
                    neighborId = response.data.numbers.neighbor.id,
                    neighborConf = response.data.numbers.neighbor.confidence,
                    floorId = response.data.floor.id,
                    floorConf = response.data.floor.confidence
                )
            } ?: return ParkingResult(status = -1, message = "server response is empty.")
        } else {
            return ParkingResult(status = -1, message = "request server error!")
        }
    }

}