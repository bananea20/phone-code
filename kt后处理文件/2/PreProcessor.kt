package com.skyui.aiengine.libcarport.module

import android.annotation.SuppressLint
import com.skyui.aiengine.libcarport.bean.CarData
import com.skyui.aiengine.libcarport.bean.DVRImage
import com.skyui.aiengine.libcore.AISDK
import com.skyui.aiengine.libutils.IoUtils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class PreProcessor(isLoadFromVideo: Boolean = true) {
    companion object {
        const val TAG = "PreProcessor"
    }

    var isLoadFromVideo = true

    init {
        this.isLoadFromVideo = isLoadFromVideo
    }

    fun process(path: String): CarData {
        if (isLoadFromVideo) return parseInfoFromVideoTar(path)
        return parseInfoFromImageTar(path)
    }

    private fun extractImagesFromVideo(path: String, tmpDir: String): List<List<String>> {
        var result: MutableList<MutableList<String>> = mutableListOf()
        AISDK.logger()?.i(TAG, "extractImagesFromVideo get images from $path")
        val s = path.split(File.separator).last()
        if (!s.startsWith("FINDCAR")) return result
        val lst = s.split(".")[0].split("_")
        val cam = lst[1]
        if (cam !in listOf("MAIN", "FRONT", "BACK", "LEFT", "RIGHT")) {
            return result
        }
        val startDate = LocalDateTime.parse(
            lst.subList(2, 9).joinToString("_"),
            DateTimeFormatter.ofPattern("yyyy_M_d_H_m_s_SSS")
        )
        val cap = VideoCapture(path, Videoio.CAP_ANDROID)
        if (!cap.isOpened) {
            AISDK.logger()?.i(TAG, "extractImagesFromVideo open video failed: $path")
            return result
        }
        val fps = cap.get(Videoio.CAP_PROP_FPS)
        if (fps <= 0 || fps > 500) {
            AISDK.logger()?.i(TAG, "extractImagesFromVideo get invalid fps $fps from $path")
            return result
        }
        var index = 0
        while (true) {
            var img = Mat()
            val flag = cap.read(img)
            if (!flag) break
            val addSec = index / fps
            val curDate = startDate.plusNanos((addSec * 1e9).toLong())
            val k = DateTimeFormatter.ofPattern("yyyy-M-d-H-m-s-SSS").format(curDate)
            val name = k.replace("-", "_")
            val out = tmpDir + File.separator + cam + "_" + name + ".jpg"
            if (!Imgcodecs.imwrite(out, img)) {
                AISDK.logger()?.i(TAG, "extractImagesFromVideo save image failed: $out")
            }
            val e = mutableListOf(k, out, cam)
            result.add(e)
            index++
        }
        AISDK.logger()?.i(TAG, "extractImagesFromVideo load ${result.size} images from $path")
        return result
    }

    @SuppressLint("LongMethod")
    fun parseInfoFromVideoTar(path: String): CarData {
        val videoSuffix = ".ts"
        AISDK.logger()?.i(TAG, "parseInfoFromVideoTar $path")
        var tmpDir = AISDK.appContext().filesDir.absolutePath + File.separator + "TarTmp"
        tmpDir += File.separator + (0..1000000).random()
        var res = CarData()
        try {
            val dir = File(tmpDir)
            if (!dir.exists())
                dir.mkdirs()
            val lst = path.split(File.separator).last().split("_")
            res.vid = lst[0]
            res.stamp = lst[1].split(".")[0]
            res.tmpDir = tmpDir
            AISDK.logger()?.i(TAG, "parseInfoFromVideoTar tmpDir = $tmpDir")
            IoUtils.doUnTarGz(path, tmpDir)
            var key2lst = hashMapOf<String, MutableList<Pair<String, String>>>()
            Files.walk(Paths.get(tmpDir))
                .filter { Files.isRegularFile(it) }
                .forEach {
                    val s = it.name
                    if (!s.endsWith(videoSuffix)) return@forEach
                    val lst = extractImagesFromVideo(it.absolutePathString(), tmpDir)
                    for (e in lst) {
                        val k = e[0]
                        val v = Pair(e[1], e[2])
                        if (k in key2lst) {
                            key2lst[k]?.add(v)
                        } else {
                            key2lst[k] = mutableListOf(v)
                        }
                    }
                }
            if (key2lst.isNotEmpty()) {
                for ((k, ls) in key2lst) {
                    val l = LocalDate.parse(
                        k,
                        DateTimeFormatter.ofPattern("yyyy-M-d-H-m-s-SSS")
                    )
                    val unix = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
                    var d = DVRImage()
                    d.stamp = unix.toDouble()
                    for (e in ls) {
                        when (e.second) {
                            "FRONT" -> d.front = e.first
                            "BACK" -> d.back = e.first
                            "LEFT" -> d.left = e.first
                            "RIGHT" -> d.right = e.first
                            else -> d.main = e.first
                        }
                    }
                    res.images += d
                }
            }
        } catch (e: Exception) {
            AISDK.logger()?.e(TAG, "parseInfoFromVideoTar", e)
        }
        return res
    }

    @SuppressLint("LongMethod")
    fun parseInfoFromImageTar(path: String): CarData {
        AISDK.logger()?.i(TAG, "parseInfoFromImageTar $path")
        var tmpDir = AISDK.appContext().filesDir.absolutePath + File.separator + "ZipTmp"
        tmpDir += File.separator + (0..1000000).random()
        var res = CarData()
        try {
            val dir = File(tmpDir)
            if (!dir.exists())
                dir.mkdirs()
            val lst = path.split(File.separator).last().split("_")
            res.vid = lst[0]
            res.stamp = lst[1].split(".")[0]
            res.tmpDir = tmpDir
            IoUtils.doUnTarGz(path, tmpDir)

            var key2lst = hashMapOf<String, List<Pair<String, String>>>()
            var chassisList = listOf<Pair<String, String>>()
            Files.walk(Paths.get(tmpDir))
                .filter { Files.isRegularFile(it) }
                .forEach {
                    val s = it.name
                    if (!s.endsWith(".jpg")) return@forEach
                    if (s.startsWith("DVR")) {
                        val lst = s.split(".")[0].split("_")
                        val cam = lst[1]
                        if (cam !in listOf("MAIN", "FRONT", "BACK", "LEFT", "RIGHT")) {
                            return@forEach
                        }
                        val k = lst.subList(2, 8).joinToString("-")
                        val v = Pair(it.absolutePathString(), cam)
                        if (k in key2lst) {
                            key2lst[k] = key2lst[k]?.plusElement(v)!!
                        } else {
                            key2lst[k] = listOf(v)
                        }
                    } else {
                        chassisList += Pair(it.absolutePathString(), s.split(".")[0])
                    }
                }
            if (key2lst.isNotEmpty()) {
                for ((k, ls) in key2lst) {
                    val l = LocalDate.parse(
                        k,
                        DateTimeFormatter.ofPattern("yyyy-M-d-H-m-s")
                    )
                    val unix = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
                    var d = DVRImage()
                    d.stamp = unix.toDouble()
                    for (e in ls) {
                        when (e.second) {
                            "FRONT" -> d.front = e.first
                            "BACK" -> d.back = e.first
                            "LEFT" -> d.left = e.first
                            "RIGHT" -> d.right = e.first
                            else -> d.main = e.first
                        }
                    }
                    res.images += d
                }
            } else {
                for (e in chassisList) {
                    val l = LocalDate.parse(
                        e.second,
                        DateTimeFormatter.ofPattern("yyyy-M-d-H-m-s")
                    )
                    val unix = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
                    var d = DVRImage()
                    d.stamp = unix.toDouble()
                    d.chassis = e.first
                }
            }
        } catch (e: Exception) {
            AISDK.logger()?.e(TAG, "parseInfoFromImageTar", e)
        }
        return res
    }
}