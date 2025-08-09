package com.jitinsharma.worker.models

import kotlinx.serialization.Serializable

@Serializable
data class CallForPapersResponse(
    val start: String? = null,
    val end: String? = null,
    val site: String? = null
)

@Serializable
data class ConferenceResponse(
    val name: String,
    val website: String,
    val location: String,
    val dateStart: String,
    val dateEnd: String,
    val cfp: CallForPapersResponse? = null
)