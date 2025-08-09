package com.jitinsharma.worker.ui.conferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jitinsharma.worker.models.AITask
import com.jitinsharma.worker.models.ConferenceModel
import com.jitinsharma.worker.network.ConferenceApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConferencesUiState(
    val conferences: List<ConferenceModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
    val aiSummary: AITask? = null,
    val isLoadingAiSummary: Boolean = false,
    val aiSummaryError: String? = null,
    val selectedConferenceForAi: ConferenceModel? = null
)

class ConferencesViewModel : ViewModel() {
    private val apiService = ConferenceApiService()
    private val _uiState = MutableStateFlow(ConferencesUiState())
    val uiState: StateFlow<ConferencesUiState> = _uiState.asStateFlow()

    init {
        loadConferences()
    }

    fun loadConferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            apiService.getTransformedConferences()
                .onSuccess { conferences ->
                    _uiState.value = _uiState.value.copy(
                        conferences = conferences,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    fun refreshConferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                errorMessage = null
            )

            apiService.getTransformedConferences()
                .onSuccess { conferences ->
                    _uiState.value = _uiState.value.copy(
                        conferences = conferences,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = exception.message ?: "Failed to refresh"
                    )
                }
        }
    }

    fun loadAiSummary(conference: ConferenceModel) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedConferenceForAi = conference,
                isLoadingAiSummary = true,
                aiSummaryError = null,
                aiSummary = null
            )

            apiService.getAISummary(conference.website)
                .onSuccess { aiResponse ->
                    _uiState.value = _uiState.value.copy(
                        aiSummary = aiResponse,
                        isLoadingAiSummary = false,
                        aiSummaryError = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAiSummary = false,
                        aiSummaryError = exception.message ?: "Failed to load AI summary"
                    )
                }
        }
    }

    fun retryAiSummary() {
        val conference = _uiState.value.selectedConferenceForAi
        if (conference != null) {
            loadAiSummary(conference)
        }
    }

    fun clearAiSummary() {
        _uiState.value = _uiState.value.copy(
            selectedConferenceForAi = null,
            aiSummary = null,
            aiSummaryError = null,
            isLoadingAiSummary = false
        )
    }
}