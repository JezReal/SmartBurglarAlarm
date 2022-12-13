package io.github.jezreal.smartburglaralarm.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlarmStatus(
    @Json(name = "alarm_status")
    val alarmStatus: String
)
