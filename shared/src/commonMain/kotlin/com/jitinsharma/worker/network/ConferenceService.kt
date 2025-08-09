package com.jitinsharma.worker.network

import com.jitinsharma.worker.models.ConferenceResponse
import io.ktor.client.call.*
import io.ktor.client.request.*

class ConferenceService {
    private val httpClient = HttpClient.instance
    
    suspend fun getUpcomingConferences(): List<ConferenceResponse> {
        return httpClient
            .get("https://androidstudygroup.github.io/conferences/upcoming.json")
            .body()
    }
}
