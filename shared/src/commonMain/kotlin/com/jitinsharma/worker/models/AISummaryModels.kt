package com.jitinsharma.worker.models

import kotlinx.serialization.Serializable

@Serializable
data class AIPrompt(
    val prompt: String
)

@Serializable
data class AIResponse(
    val response: String
)

@Serializable
data class AITask(
    val inputs: AIPrompt,
    val response: AIResponse
)

@Serializable
data class AIRequest(
    val url: String
)