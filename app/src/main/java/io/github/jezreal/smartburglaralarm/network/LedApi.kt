package io.github.jezreal.smartburglaralarm.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface LedApi {
    @GET("/random")
    suspend fun getRandomQuote() : Response<LedStatus>

    @POST("/toggle")
    suspend fun toggleLed(): Response<LedStatus>

    companion object {
        const val BASE_URL = "http://192.168.4.1:8080"
    }
}