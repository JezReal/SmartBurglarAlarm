package io.github.jezreal.smartburglaralarm.repository

import io.github.jezreal.smartburglaralarm.network.LedApi
import io.github.jezreal.smartburglaralarm.network.LedStatus
import io.github.jezreal.smartburglaralarm.wrappers.Resource
import java.net.UnknownHostException
import javax.inject.Inject

class TestApiRepository @Inject constructor(
    private val api: LedApi
) {
    suspend fun toggleLed(): Resource<LedStatus> {
        return try {
            val response = api.toggleLed()
            val result = response.body()

            if (response.isSuccessful && result != null) {
                Resource.Success(result)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: UnknownHostException) {
            Resource.Error("Unreachable host")
        } catch (e: Exception) {
            Resource.Error(e.message!!)
        }
    }
}