package com.jitinsharma.worker.models

import kotlinx.serialization.Serializable

@Serializable
data class ConferenceModel(
    val name: String,
    val website: String,
    val city: String,
    val country: String,
    val countryFlagUrl: String,
    val dateStartEpoch: Long,
    val dateEndEpoch: Long,
    val cfp: CallForPapersModel? = null
)

@Serializable
data class CallForPapersModel(
    val startEpoch: Long? = null,
    val endEpoch: Long? = null,
    val site: String? = null
)

