package com.jitinsharma.worker.network

import com.jitinsharma.worker.models.AIRequest
import com.jitinsharma.worker.models.ConferenceModel
import com.jitinsharma.worker.models.AITask
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ConferenceApiService {
    private val httpClient = HttpClient.instance
    private val baseUrl = ""

    suspend fun getTransformedConferences(): Result<List<ConferenceModel>> {
        return try {

            val conferences: List<ConferenceModel> = httpClient
                .get(baseUrl)
                .body()
            Result.success(conferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAISummary(conferenceUrl: String): Result<AITask> {
        return try {
            val request = AIRequest(url = conferenceUrl)
            val response = httpClient.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            val aiResponse = response.body<AITask>()
            Result.success(aiResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
