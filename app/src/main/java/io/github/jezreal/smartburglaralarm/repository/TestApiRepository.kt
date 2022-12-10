package io.github.jezreal.smartburglaralarm.repository

import io.github.jezreal.smartburglaralarm.network.Quote
import io.github.jezreal.smartburglaralarm.network.QuoteApi
import io.github.jezreal.smartburglaralarm.wrappers.Resource
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

class TestApiRepository @Inject constructor(
    private val api: QuoteApi
) {
    suspend fun getApiResponse(): Resource<Quote> {
        return try {
            val response = api.getRandomQuote()
            val result = response.body()

            if (response.isSuccessful && result != null) {
                Timber.d("Should be successfull")
                Resource.Success(result)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: UnknownHostException) {
            Resource.Error("No internet")
        } catch (e: Exception) {
            Resource.Error(e.message!!)
        }
    }
}