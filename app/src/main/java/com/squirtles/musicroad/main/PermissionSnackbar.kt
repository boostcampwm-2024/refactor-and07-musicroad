package com.squirtles.musicroad.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.squirtles.musicroad.R

@Composable
fun PermissionSnackbar(
    onActionPerformed: () -> Unit,
    onDismissed: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        val result = snackbarHostState
            .showSnackbar(
                message = context.getString(R.string.snackbar_text),
                actionLabel = context.getString(R.string.action_to_setting),
                duration = SnackbarDuration.Indefinite
            )

        when (result) {
            SnackbarResult.ActionPerformed -> {
                onActionPerformed()
            }

            SnackbarResult.Dismissed -> {
                onDismissed()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
        )
    }
}
