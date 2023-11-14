package com.skyui.aiengine.libcarport.module

import androidx.annotation.Keep

@Keep
data class ServerResponse(
    val data: Data,
    val request_id: String,
    val resultCode: String,
    val serverTime: Long
)

@Keep
data class Data(
    val code: Int,
    val floor: Floor,
    val message: String,
    val numbers: Numbers
) {
    @Keep
    data class Floor(
        val confidence: Float,
        val id: String
    )

    @Keep
    data class Numbers(
        val master: Master,
        val neighbor: Neighbor
    )

    @Keep
    data class Master(
        val confidence: Float,
        val id: String
    )

    @Keep
    data class Neighbor(
        val confidence: Float,
        val id: String
    )
}