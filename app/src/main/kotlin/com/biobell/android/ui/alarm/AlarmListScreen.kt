package com.biobell.android.ui.alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobell.android.domain.model.Alarm
import com.biobell.android.ui.components.AlarmCard
import com.biobell.android.ui.components.EmptyAlarmsState

/**
 * Alarm list screen — BioBell's home screen.
 *
 * Features:
 * - Live-updating list of all alarms (Room Flow → StateFlow)
 * - Toggle alarm enabled/disabled with immediate feedback
 * - Swipe-to-delete with snackbar undo
 * - FAB to create a new alarm
 * - Empty state with friendly onboarding text
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    onCreateAlarm: () -> Unit = {},
    onEditAlarm: (Long) -> Unit = {},
    viewModel: AlarmListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BioBell",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateAlarm,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create alarm",
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                // Loading
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                // Empty state
                uiState.isEmpty -> {
                    EmptyAlarmsState(modifier = Modifier.fillMaxSize())
                }

                // Alarm list
                else -> {
                    AlarmList(
                        alarms = uiState.alarms,
                        onToggle = { alarm, enabled ->
                            viewModel.onToggleAlarm(alarm, enabled)
                        },
                        onEdit = { alarm -> onEditAlarm(alarm.id) },
                        onDelete = { alarm ->
                            viewModel.onDeleteAlarm(alarm)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AlarmList(
    alarms: List<Alarm>,
    onToggle: (Alarm, Boolean) -> Unit,
    onEdit: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 88.dp, // FAB clearance
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(
            items = alarms,
            key = { alarm -> alarm.id },
        ) { alarm ->
            // Track deletion state for swipe-to-delete
            var isDeleted by remember { mutableStateOf(false) }

            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart ||
                        value == SwipeToDismissBoxValue.StartToEnd
                    ) {
                        isDeleted = true
                        onDelete(alarm)
                        true
                    } else false
                },
            )

            AnimatedVisibility(
                visible = !isDeleted,
                enter = fadeIn(tween(150)) + slideInVertically { -it / 4 },
                exit = fadeOut(tween(150)),
                modifier = Modifier.animateItemPlacement(),
            ) {
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = { SwipeDismissBackground(dismissState) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { enabled -> onToggle(alarm, enabled) },
                        onEdit = { onEdit(alarm) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeDismissBackground(state: SwipeToDismissBoxState) {
    val color = MaterialTheme.colorScheme.errorContainer
    val icon = Icons.Outlined.Delete

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        contentAlignment = when (state.dismissDirection) {
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            else -> Alignment.CenterEnd
        },
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large,
            color = color,
        ) {
            Box(
                contentAlignment = when (state.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                },
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}
