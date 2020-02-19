package io.nemiron.spyapplication.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpyData (
    @Json(name = "date")
    val date: String,

    @Json(name = "memory")
    val memory: Int,

    @Json(name = "battery")
    val battery: Int
)
