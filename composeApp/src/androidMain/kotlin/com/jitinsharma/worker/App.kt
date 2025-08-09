package com.jitinsharma.worker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.jitinsharma.worker.ui.conferences.ConferencesScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        ConferencesScreen(
            modifier = Modifier
                .fillMaxSize()
        )
    }
}