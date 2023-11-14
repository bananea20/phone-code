package com.skyui.aiengine.libcarport.module

import android.graphics.Bitmap
import com.skyui.aiengine.api.bean.OcrResult
import com.skyui.aiengine.api.callback.OcrInitCallback
import com.skyui.aiengine.api.callback.OcrResultCallback
import com.skyui.aiengine.libaiproxy.AiEngine
import com.skyui.aiengine.libcore.AIException
import com.skyui.aiengine.libcore.AISDK
import com.skyui.aiengine.libcore.APPScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class ThirdPartyOCR(isRotate: Boolean = true) : AbsCarportOCR() {
    override fun init() {
        val lock = Object()
        APPScope.launch {
            val application = AISDK.appContext()
            AiEngine.init(application)
            AiEngine.getCommonOcr().ocrInit(LEVEL, object : OcrInitCallback {
                override fun onSuccess(result: String) {
                    AISDK.logger()?.i(Companion.TAG, "init onSuccess = $result")
                    synchronized(lock) {
                        lock.notify()
                    }
                }

                override fun onFailed(errorCode: Int, exception: AIException) {
                    AISDK.logger()?.e(Companion.TAG, "init failed, code = $errorCode", exception)
                    synchronized(lock) {
                        lock.notify()
                    }
                }
            })
        }
        synchronized(lock) {
            lock.wait()
        }
    }

    override suspend fun invoke(images: List<Bitmap>): OCROutput {
        val lock = Object()
        var output = OCROutput()
        val ocrCallback = object : OcrResultCallback {
            override fun onSuccess(result: OcrResult) {
                AISDK.logger()?.i(TAG, "reg onSuccess = ${result.originResult}")
                try {
                    val jsonObj = JSONObject(result.originResult)
                    val jsonArray = jsonObj.getJSONArray("lines")
                    val width = jsonObj.getInt("width")
                    val height = jsonObj.getInt("height")
                    var lst: List<Text> = emptyList()
                    for (i in 0 until jsonArray.length()) {
                        val obj: JSONObject = jsonArray.getJSONObject(i)
                        var t = Text()
                        val boxArr = obj.getJSONArray("poly")
                        t.value = obj.getString("text")
                        t.confidence = obj.getDouble("score").toFloat()
                        t.height = height
                        t.width = width
                        for (j in 0 until 4) {
                            val idx = (4 - j) % 4
                            t.box += Point(
                                x = boxArr.getInt(idx * 2).toFloat(),
                                y = boxArr.getInt(idx * 2 + 1).toFloat()
                            )
                        }
                        lst += t
                    }
                    output.data = output.data.plusElement(lst)
                } catch (e: Exception) {
                    AISDK.logger()?.e(TAG, "reg parse ocr result error ${e.message}", e)
                }
                synchronized(lock) {
                    lock.notify()
                }
            }

            override fun onFailed(errorCode: Int, exception: AIException) {
                AISDK.logger()?.e(TAG, "reg failed, code = $errorCode", exception)
                synchronized(lock) {
                    lock.notify()
                }
            }
        }
        for (b in images) {
            val bs = if (isRotate) {
                rotateBitmap(b)
            } else {
                listOf(b)
            }
            for (bmp in bs) {
                val stream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteForPhoto = stream.toByteArray()
                AiEngine.getCommonOcr().ocrRecognitionByte(0, byteForPhoto, ocrCallback)
                synchronized(lock) {
                    lock.wait()
                }
            }
        }
        return output
    }

    init {
        this.isRotate = isRotate
        init()
    }

    companion object {
        const val TAG = "ThirdPartyOCR"
        const val LEVEL = "low" // low/mid/high
    }
}