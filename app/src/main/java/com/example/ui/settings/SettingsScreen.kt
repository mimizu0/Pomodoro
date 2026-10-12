package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.model.PomodoroSettings
import com.example.ui.components.NumberInputDialog

const val MAX_LONG_BREAK_MINUTES = 60

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: PomodoroSettings,
    onUpdateSettings: (PomodoroSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showLongBreakDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "設定",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Theme Mode Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "テーマ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = settings.themeMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateSettings(settings.copy(themeMode = mode)) },
                            label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                val icon = when (mode) {
                                    AppThemeMode.SYSTEM -> Icons.Filled.SettingsSuggest
                                    AppThemeMode.LIGHT -> Icons.Filled.LightMode
                                    AppThemeMode.DARK -> Icons.Filled.DarkMode
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_chip_${mode.name.lowercase()}")
                        )
                    }
                }
            }

            HorizontalDivider()

            // Long break duration (changes apply immediately, no save button)
            Text(
                text = "時間設定",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            SettingDurationRow(
                icon = { Icon(Icons.Filled.Snooze, contentDescription = null) },
                title = "ロング休憩時間",
                valueMins = settings.longBreakDurationMinutes,
                maxMins = MAX_LONG_BREAK_MINUTES,
                unit = "分",
                testTagPrefix = "setting_long_break_mins",
                onDecrease = { onUpdateSettings(settings.copy(longBreakDurationMinutes = settings.longBreakDurationMinutes - 1)) },
                onIncrease = { onUpdateSettings(settings.copy(longBreakDurationMinutes = settings.longBreakDurationMinutes + 1)) },
                onOpenDialog = { showLongBreakDialog = true }
            )

            HorizontalDivider()

            // Phase transition settings section
            Text(
                text = "作業と休憩の切り替え",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            SettingToggleRow(
                icon = { Icon(Icons.Filled.Coffee, contentDescription = null) },
                title = "休憩の自動開始",
                subtitle = "作業が終わると休憩を開始",
                checked = settings.autoStartBreak,
                onCheckedChange = { onUpdateSettings(settings.copy(autoStartBreak = it)) },
                testTag = "auto_start_break_toggle"
            )

            SettingToggleRow(
                icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                title = "作業の自動開始",
                subtitle = "休憩が終わると次の作業を開始",
                checked = settings.autoStartWork,
                onCheckedChange = { onUpdateSettings(settings.copy(autoStartWork = it)) },
                testTag = "auto_start_work_toggle"
            )

            SettingToggleRow(
                icon = { Icon(Icons.Filled.HourglassTop, contentDescription = null) },
                title = "作業の継続",
                subtitle = "時間を過ぎても「次へ」を押すまで計測",
                checked = settings.continueWorkUntilManual,
                onCheckedChange = { onUpdateSettings(settings.copy(continueWorkUntilManual = it)) },
                testTag = "continue_work_until_manual_toggle"
            )

            SettingToggleRow(
                icon = { Icon(Icons.Filled.Coffee, contentDescription = null) },
                title = "休憩の継続",
                subtitle = "時間を過ぎても「次へ」を押すまで計測",
                checked = settings.continueBreakUntilManual,
                onCheckedChange = { onUpdateSettings(settings.copy(continueBreakUntilManual = it)) },
                testTag = "continue_break_until_manual_toggle"
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showLongBreakDialog) {
        NumberInputDialog(
            title = "ロング休憩時間",
            initialValue = settings.longBreakDurationMinutes,
            maxValue = MAX_LONG_BREAK_MINUTES,
            unit = "分",
            onConfirm = { onUpdateSettings(settings.copy(longBreakDurationMinutes = it)) },
            onDismiss = { showLongBreakDialog = false }
        )
    }
}

@Composable
private fun SettingDurationRow(
    icon: @Composable () -> Unit,
    title: String,
    valueMins: Int,
    maxMins: Int,
    unit: String,
    testTagPrefix: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onOpenDialog: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "タップで直接入力",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { if (valueMins > 1) onDecrease() },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${testTagPrefix}_minus")
            ) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "減らす",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { onOpenDialog() }
            ) {
                Text(
                    text = "$valueMins$unit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("${testTagPrefix}_value")
                )
            }

            IconButton(
                onClick = { if (valueMins < maxMins) onIncrease() },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${testTagPrefix}_plus")
            ) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "増やす",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
