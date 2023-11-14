package com.skyui.aiengine.libcarport.module

import com.skyui.aiengine.libcarport.CarportModuleBuilder
import com.skyui.aiengine.libcore.AISDK
import com.skyui.aiengine.libmodule.modules.AbsTfliteController
import com.skyui.aiengine.libmodule.modules.ModuleData
import java.io.File

/**
 * @author : create by owen.ou
 * 版本 1.0
 * 创建日期: 2023/1/9
 * 描述: 寻车OCR算法控制器，内部含两个c++ tflite模型
 */
class CarportNativeController : AbsTfliteController<String, String>() {

    private val isModelFromSDCard = true

    override fun getControllerKey(): String {
        return MODULE_KEY
    }

    private fun getModelPath(key: String): String {
        AISDK.logger()?.i(TAG, "is load model from sd card: $isModelFromSDCard")
        val baseDir = AISDK.appContext().filesDir.absolutePath + File.separator + "OCRModels"
        val det = baseDir + File.separator + "det.model"
        val cls = baseDir + File.separator + "classification.model"
        val rec = baseDir + File.separator + "recognition.model"
        val path = when (key) {
            "det" -> if (isModelFromSDCard && File(det).exists()) det else "assets:det.model"
            "cls" -> if (isModelFromSDCard && File(cls).exists()) cls else "assets:classification.model"
            "rec" -> if (isModelFromSDCard && File(rec).exists()) rec else "assets:recognition.model"
            else -> ""
        }
        AISDK.logger()?.i(TAG, "load $key model from $path")
        return path
    }

    override val moduleList: List<ModuleData>
        get() = listOf(
            ModuleData(
                key = "module_carport_detection",
                path = getModelPath("det"),
                useGpu = false,
            ),
            ModuleData(
                key = "module_carport_recognition",
                path = getModelPath("rec"),
                useGpu = false,
            ),
            ModuleData(
                key = "module_carport_classification",
                path = getModelPath("cls"),
                useGpu = false,
            )
        )

    override suspend fun runAction(data: String): String? {
        super.runAction(data)
        val result = runController(nativeRef, data)
        AISDK.logger()?.i(TAG, "controller result = $result")
        return result
    }

    init {
        builder = CarportModuleBuilder.builder
    }

    companion object {
        private const val MODULE_KEY = "native_controller_carport"
        val instance: CarportNativeController by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CarportNativeController() }

        init {
            System.loadLibrary("carport")
        }
    }

    override var autoReleaseDelay: Long = 0

}