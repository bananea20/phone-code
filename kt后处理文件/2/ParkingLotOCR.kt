package com.skyui.aiengine.libcarport.module

import android.graphics.Bitmap
import com.skyui.aiengine.libcore.AISDK
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*


class ParkingLotOCR(isRotate: Boolean = true) : AbsCarportOCR() {
    override fun init() {

    }

    override suspend fun invoke(images: List<Bitmap>): OCROutput {
        var output = OCROutput()
        for (b in images) {
            val bs = if (isRotate) {
                rotateBitmap(b)
            } else {
                listOf(b)
            }
            for (bmp in bs) {
                try {
                    val stream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val byteForPhoto = stream.toByteArray()
                    val encoder: Base64.Encoder = Base64.getEncoder()
                    val img = encoder.encodeToString(byteForPhoto)
                    val req = "{\"type\":0, \"images\":[\"$img\"]}"
                    val resp = CarportNativeController.instance.run(req)
                    val jsonObj = JSONObject(resp)
                    val jsonArray = jsonObj.getJSONArray("ocr").getJSONArray(0)
                    var lst: List<Text> = emptyList()
                    for (i in 0 until jsonArray.length()) {
                        val obj: JSONObject = jsonArray.getJSONObject(i)
                        var t = Text()
                        val boxArr = obj.getJSONArray("points")
                        t.value = obj.getString("text")
                        t.direction = obj.getString("direction")
                        t.confidence = obj.getDouble("confidence").toFloat()
                        t.height = obj.getInt("height")
                        t.width = obj.getInt("width")
                        for (j in 0 until 4) {
                            val b = boxArr.getJSONArray(j)
                            t.box += Point(
                                x = b.getInt(0).toFloat(),
                                y = b.getInt(1).toFloat()
                            )
                        }
                        lst += t
                    }
                    output.data = output.data.plusElement(lst)
                } catch (e: Exception) {
                    AISDK.logger()?.e(TAG, "native ocr failed: ${e.message}", e)
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
        const val TAG = "ParkingLotOCR"
    }
}