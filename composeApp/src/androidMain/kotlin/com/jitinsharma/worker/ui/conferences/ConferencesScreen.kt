package com.jitinsharma.worker.ui.conferences

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jitinsharma.worker.ui.components.ConferenceCard
import com.jitinsharma.worker.ui.components.AISummaryBottomSheet
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConferencesScreen(
    viewModel: ConferencesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAiSummarySheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Android Developer Conferences",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = { viewModel.refreshConferences() }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        )

        // Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.conferences.isEmpty() -> {
                    // Initial Loading State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading conferences...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                uiState.errorMessage != null && uiState.conferences.isEmpty() -> {
                    // Error State (no data to show)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "😔",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Something went wrong",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "err",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadConferences() }
                        ) {
                            Text("Retry")
                        }
                    }
                }

                uiState.conferences.isEmpty() -> {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📅",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No conferences found",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Check back later for upcoming events",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    // Conference List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.conferences,
                            key = { conference -> "${conference.name}-${conference.dateStartEpoch}" }
                        ) { conference ->
                            ConferenceCard(
                                conference = conference,
                                onClick = {
                                    // Open conference website
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, conference.website.toUri())
                                        context.startActivity(intent)
                                    } catch (_: Exception) { }
                                },
                                onAiSummaryClick = {
                                    viewModel.loadAiSummary(conference)
                                    showAiSummarySheet = true
                                }
                            )
                        }
                    }
                }
            }

            // Error Snackbar (when there's data to show)
            uiState.errorMessage?.let { errorMessage ->
                if (uiState.conferences.isNotEmpty()) {
                    LaunchedEffect(errorMessage) {
                        // Show snackbar for errors when we have data
                    }
                }
            }

            // Loading overlay for refresh
            if (uiState.isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Refreshing...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
    
    // AI Summary Bottom Sheet
    uiState.selectedConferenceForAi?.let { selectedConference ->
        AISummaryBottomSheet(
            conference = selectedConference,
            uiState = uiState,
            isVisible = showAiSummarySheet,
            onDismiss = {
                showAiSummarySheet = false
                viewModel.clearAiSummary()
            },
            onRetry = {
                viewModel.retryAiSummary()
            }
        )
    }
}