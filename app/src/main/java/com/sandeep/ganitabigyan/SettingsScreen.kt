// SettingsScreen.kt

package com.sandeep.ganitabigyan

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.utils.toOdiaNumerals
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val isVibrationEnabled by settingsViewModel.isVibrationEnabled.collectAsStateWithLifecycle()
    val morningTime by settingsViewModel.morningReminderTime.collectAsStateWithLifecycle()
    val eveningTime by settingsViewModel.eveningReminderTime.collectAsStateWithLifecycle()

    val timePickerStateMorning = rememberTimePickerState(
        initialHour = morningTime.split(":")[0].toInt(),
        initialMinute = morningTime.split(":")[1].toInt(),
        is24Hour = false
    )
    val timePickerStateEvening = rememberTimePickerState(
        initialHour = eveningTime.split(":")[0].toInt(),
        initialMinute = eveningTime.split(":")[1].toInt(),
        is24Hour = false
    )
    var showMorningTimePicker by remember { mutableStateOf(false) }
    var showEveningTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ସେଟିଂସ") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsCategory(title = "ସାଧାରଣ")
            SettingsSwitchItem(
                title = "ଭାଇବ୍ରେସନ୍",
                description = "ଭୁଲ୍ ଉତ୍ତର ପାଇଁ ଫିଡବ୍ୟାକ୍",
                icon = Icons.Default.Vibration,
                checked = isVibrationEnabled,
                onCheckedChange = { settingsViewModel.setVibrationEnabled(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsCategory(title = "ରିମାଇଣ୍ଡର")
            SettingsClickableItem(
                title = "ସକାଳ ରିମାଇଣ୍ଡର",
                description = formatTimeInOdia(morningTime),
                icon = Icons.Default.Notifications,
                onClick = { showMorningTimePicker = true }
            )
            SettingsClickableItem(
                title = "ସନ୍ଧ୍ୟା ରିମାଇଣ୍ଡର",
                description = formatTimeInOdia(eveningTime),
                icon = Icons.Default.Notifications,
                onClick = { showEveningTimePicker = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsCategory(title = "ଅନ୍ୟାନ୍ୟ")
            SettingsClickableItem(
                title = "ଆପ୍ ବିଷୟରେ",
                description = "ସଂସ୍କରଣ ଏବଂ ସୂଚନା",
                icon = Icons.Default.Info,
                onClick = { navController.navigate(AppDestinations.ABOUT_ROUTE) }
            )
        }
    }

    if (showMorningTimePicker) {
        TimePickerDialog(
            onDismiss = { showMorningTimePicker = false },
            onConfirm = {
                val newTime = String.format("%02d:%02d", timePickerStateMorning.hour, timePickerStateMorning.minute)
                settingsViewModel.setMorningReminderTime(newTime)
                showMorningTimePicker = false
                Toast.makeText(context, "ସକାଳ ରିମାଇଣ୍ଡର ସେଟ୍ ହେଲା", Toast.LENGTH_SHORT).show()
            },
            state = timePickerStateMorning
        )
    }

    if (showEveningTimePicker) {
        TimePickerDialog(
            onDismiss = { showEveningTimePicker = false },
            onConfirm = {
                val newTime = String.format("%02d:%02d", timePickerStateEvening.hour, timePickerStateEvening.minute)
                settingsViewModel.setEveningReminderTime(newTime)
                showEveningTimePicker = false
                Toast.makeText(context, "ସନ୍ଧ୍ୟା ରିମାଇଣ୍ଡର ସେଟ୍ ହେଲା", Toast.LENGTH_SHORT).show()
            },
            state = timePickerStateEvening
        )
    }
}

@Composable
private fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// This is the standard Material 3 Time Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    state: TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("ଠିକ୍ ଅଛି")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ବାତିଲ କରନ୍ତୁ")
            }
        },
        text = {
            TimePicker(state = state)
        }
    )
}

private fun formatTimeInOdia(time24h: String): String {
    return try {
        val sdf24h = SimpleDateFormat("HH:mm", Locale.US)
        val sdf12h = SimpleDateFormat("hh:mm a", Locale("en", "IN"))
        val date = sdf24h.parse(time24h)
        val formattedTime = sdf12h.format(date!!)
        // Just convert the numbers, leave AM/PM as is.
        formattedTime.toOdiaNumerals()
    } catch (e: Exception) {
        time24h
    }
}