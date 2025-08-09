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

    suspend fun getTransformedConferences(): Result<List<ConferenceModel>> {
        return try {
            val conferences: List<ConferenceModel> = httpClient
                .get("https://kotlin-worker-multiplatform.jitins2892.workers.dev")
                .body()
            Result.success(conferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAISummary(conferenceUrl: String): Result<AITask> {
        return try {
            val request = AIRequest(url = conferenceUrl)
            val response = httpClient.post("https://kotlin-worker-multiplatform.jitins2892.workers.dev/") {
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
