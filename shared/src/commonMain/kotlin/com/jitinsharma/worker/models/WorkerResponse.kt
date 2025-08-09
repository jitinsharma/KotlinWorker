package com.jitinsharma.worker.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class WorkerResponse(
    val message: String,
    val timestamp: Long,
    val status: String
)

fun WorkerResponse.toJson(): String = Json.encodeToString(this)