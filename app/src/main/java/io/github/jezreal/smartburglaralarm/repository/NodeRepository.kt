package io.github.jezreal.smartburglaralarm.repository

import io.github.jezreal.smartburglaralarm.network.AlarmStatus
import io.github.jezreal.smartburglaralarm.network.NodeApi
import io.github.jezreal.smartburglaralarm.wrappers.Resource
import java.net.UnknownHostException
import javax.inject.Inject

class NodeRepository @Inject constructor(
    private val api: NodeApi
) {

    suspend fun connectToDevice(): Resource<AlarmStatus> {
        return try {
            val response = api.getAlarmStatus()
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

    suspend fun toggleAlarmStatus(): Resource<AlarmStatus> {
        return try {
            val response = api.toggleAlarmStatus()
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