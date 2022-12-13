package io.github.jezreal.smartburglaralarm.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface NodeApi {

    @GET("/status")
    suspend fun getAlarmStatus(): Response<AlarmStatus>

    @POST("/toggle")
    suspend fun toggleAlarmStatus(): Response<AlarmStatus>

    companion object {
        const val BASE_URL = "http://192.168.4.1:8080"
    }
}