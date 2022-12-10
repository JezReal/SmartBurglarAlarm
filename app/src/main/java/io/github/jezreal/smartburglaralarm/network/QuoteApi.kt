package io.github.jezreal.smartburglaralarm.network

import retrofit2.Response
import retrofit2.http.GET

interface QuoteApi {
    @GET("/random")
    suspend fun getRandomQuote() : Response<Quote>

    companion object {
        const val BASE_URL = "https://api.quotable.io"
    }
}