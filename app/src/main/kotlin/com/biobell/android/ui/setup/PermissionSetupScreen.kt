package com.biobell.android.ui.setup

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * First-launch permission onboarding screen.
 *
 * Walks the user through three steps:
 * 1. Exact alarm permission (SCHEDULE_EXACT_ALARM — API 31+)
 * 2. Battery optimization exemption (critical for Xiaomi/MIUI/OEM)
 * 3. Notification permission (POST_NOTIFICATIONS — API 33+)
 *
 * Each step shows:
 * - What the permission does (plain language)
 * - Current status (✓ granted / ✗ needed)
 * - A button to grant it (opens system Settings or shows runtime dialog)
 *
 * A "Continue" button at the bottom is always available — users can skip
 * non-critical permissions (only notifications is fully optional).
 */
@Composable
fun PermissionSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: PermissionSetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-check permissions every time user returns from System Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // POST_NOTIFICATIONS runtime permission launcher
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> viewModel.onNotificationPermissionResult(granted) },
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // ── Header ────────────────────────────────────────────────────
            Icon(
                imageVector = Icons.Outlined.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Set up BioBell",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A few permissions are needed for alarms to fire reliably — even when the screen is off.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Step 1: Exact alarms ──────────────────────────────────────
            AnimatedVisibility(visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionCard(
                    icon = Icons.Outlined.Alarm,
                    title = "Exact Alarms",
                    description = "Required so alarms fire at the exact minute. Without this, the system may delay or skip your alarm.",
                    isGranted = uiState.canScheduleExactAlarms,
                    grantedLabel = "Granted",
                    deniedLabel = "Allow Exact Alarms",
                    isCritical = true,
                    onGrant = { viewModel.openExactAlarmSettings() },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Step 2: Battery optimization ─────────────────────────────
            PermissionCard(
                icon = Icons.Outlined.BatteryFull,
                title = "Unrestricted Battery Usage",
                description = "Prevents the system from killing BioBell in the background. Essential on Xiaomi, Samsung, and other OEM devices with aggressive battery management.",
                isGranted = uiState.isBatteryOptimizationExempt,
                grantedLabel = "Exempted",
                deniedLabel = "Disable Battery Restriction",
                isCritical = true,
                onGrant = { viewModel.openBatteryOptimizationSettings() },
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Step 3: Notifications ─────────────────────────────────────
            AnimatedVisibility(visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionCard(
                    icon = Icons.Outlined.Notifications,
                    title = "Alarm Notifications",
                    description = "Shows the alarm notification on the lock screen and in the status bar when your alarm fires.",
                    isGranted = uiState.notificationsGranted,
                    grantedLabel = "Granted",
                    deniedLabel = "Allow Notifications",
                    isCritical = false,
                    onGrant = {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Xiaomi / MIUI note ────────────────────────────────────────
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).padding(top = 1.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "On Xiaomi/MIUI: also enable Autostart for BioBell in Settings → Apps → Manage Apps → BioBell → Autostart.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Continue button ───────────────────────────────────────────
            Button(
                onClick = { viewModel.completeSetup(onSetupComplete) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Text(
                    text = if (uiState.allCriticalGranted) "All set — Let's go!" else "Continue anyway",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    grantedLabel: String,
    deniedLabel: String,
    isCritical: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when {
        isGranted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        isCritical -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Icon column
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isGranted)
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isGranted) Icons.Outlined.CheckCircle else icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (isGranted)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Text + button column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isCritical && !isGranted) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.error,
                        ) {
                            Text(
                                text = "Required",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isGranted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            text = grantedLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    FilledTonalButton(
                        onClick = onGrant,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                    ) {
                        Text(
                            text = deniedLabel,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}
