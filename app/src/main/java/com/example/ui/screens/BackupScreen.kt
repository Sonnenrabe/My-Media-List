package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AutoBackupManager
import com.example.data.FullBackupSummary
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.components.ImportConfirmationDialog
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var autoBackupStatus by remember { mutableStateOf(AutoBackupManager.getStatus(context)) }
    var pasteInputText by remember { mutableStateOf("") }
    var importSummaryToConfirm by remember { mutableStateOf<FullBackupSummary?>(null) }
    var cachedExportContent by remember { mutableStateOf("") }

    val locale = remember(appLanguage) {
        when (appLanguage) {
            AppLanguage.ENGLISH -> Locale.ENGLISH
            AppLanguage.GERMAN -> Locale.GERMANY
            AppLanguage.SPANISH -> Locale("es", "ES")
            AppLanguage.ITALIAN -> Locale.ITALY
            AppLanguage.JAPANESE -> Locale.JAPAN
            AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
        }
    }

    // Save File Launcher (CSV)
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                coroutineScope.launch {
                    val csv = if (cachedExportContent.isNotBlank()) cachedExportContent else viewModel.exportDataAsExcelCsv()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(csv.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, strings.csvSavedToast, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, strings.saveErrorToast(e.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    // Pick Folder Launcher for Auto Backup
    val chooseFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            AutoBackupManager.setCustomBackupFolder(context, uri, null)
            autoBackupStatus = AutoBackupManager.getStatus(context)
            Toast.makeText(context, strings.folderUpdatedToast, Toast.LENGTH_SHORT).show()
        }
    }

    // Pick File Launcher for Import
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val sb = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            sb.append(line).append("\n")
                            line = reader.readLine()
                        }
                    }
                }
                val content = sb.toString()
                val parseResult = viewModel.parseBackupSummary(content)
                parseResult.onSuccess { summary ->
                    importSummaryToConfirm = summary
                }.onFailure { err ->
                    Toast.makeText(context, strings.invalidFormatToast(err.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, strings.readErrorToast(e.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    val compactButtonPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameDarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Excel-Compatible Info Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.excelCompatibleTitle,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NeonGreen
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.excelCompatibleDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // 2. Daily Auto-Backup Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            tint = if (autoBackupEnabled) NeonGreen else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.dailyAutoBackupTitle,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (autoBackupEnabled) NeonGreen.copy(alpha = 0.15f) else TextTertiary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (autoBackupEnabled) strings.activeBadge else strings.inactiveBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (autoBackupEnabled) NeonGreen else TextTertiary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = { checked ->
                                viewModel.setAutoBackupEnabled(checked)
                                autoBackupStatus = AutoBackupManager.getStatus(context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF002026),
                                checkedTrackColor = NeonGreen,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = GameDarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("auto_backup_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (autoBackupEnabled) strings.dailyAutoBackupDesc else strings.dailyAutoBackupDisabledDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (autoBackupEnabled) TextSecondary else TextTertiary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GameDarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(strings.storageLocation, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                        Text(
                            text = if (autoBackupStatus.hasCustomFolder) {
                                autoBackupStatus.folderDisplayName
                            } else {
                                strings.defaultStorageName
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.fileLabel(autoBackupStatus.lastBackupFileName),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan
                        )
                        if (autoBackupStatus.lastBackupTimestampMs > 0L) {
                            val formatted = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale)
                                .format(Date(autoBackupStatus.lastBackupTimestampMs))
                            Text(strings.lastBackupLabel(formatted), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { chooseFolderLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003844), contentColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = compactButtonPadding,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.chooseFolder,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val csv = viewModel.exportDataAsExcelCsv()
                                val result = AutoBackupManager.runDailyBackup(context, csv, force = true)
                                result.onSuccess { msg ->
                                    autoBackupStatus = AutoBackupManager.getStatus(context)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }.onFailure { err ->
                                    Toast.makeText(context, strings.saveErrorToast(err.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3B2B), contentColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = compactButtonPadding,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            text = strings.backupNow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (autoBackupStatus.hasCustomFolder) {
                    TextButton(
                        onClick = {
                            AutoBackupManager.clearCustomBackupFolder(context)
                            autoBackupStatus = AutoBackupManager.getStatus(context)
                            Toast.makeText(context, strings.defaultFolderRestoredToast, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(strings.resetDefaultFolder, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                }
            }
        }

        // 3. Manual Export Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.manualExportTitle,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                cachedExportContent = viewModel.exportDataAsExcelCsv()
                                val fileName = "game_media_backup_${System.currentTimeMillis() / 1000}.csv"
                                saveFileLauncher.launch(fileName)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF002026)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = compactButtonPadding,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.csvExportButton,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val csv = viewModel.exportDataAsExcelCsv()
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                    type = "text/csv"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, strings.shareBackupTitle))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003844), contentColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = compactButtonPadding,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.shareButton,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val csv = viewModel.exportDataAsExcelCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("My Media List Backup", csv))
                            Toast.makeText(context, strings.csvCopiedToast, Toast.LENGTH_SHORT).show()
                        }
                    },
                    contentPadding = compactButtonPadding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.copyCsvButton,
                        color = TextPrimary,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 4. Import Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.importDataTitle,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.importDataDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { openFileLauncher.launch(arrayOf("text/*", "application/*")) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo, contentColor = Color(0xFF0D1226)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = compactButtonPadding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.openBackupFileButton,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pasteInputText,
                    onValueChange = { pasteInputText = it },
                    placeholder = { Text(strings.pastePlaceholder, style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = TextTertiary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val item = clipboard.primaryClip?.getItemAt(0)
                            val text = item?.text?.toString() ?: ""
                            if (text.isNotBlank()) pasteInputText = text
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = strings.pasteDesc, tint = NeonCyan)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (pasteInputText.isNotBlank()) {
                            val parseResult = viewModel.parseBackupSummary(pasteInputText)
                            parseResult.onSuccess { summary ->
                                importSummaryToConfirm = summary
                            }.onFailure { err ->
                                Toast.makeText(context, strings.readErrorToast(err.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, strings.selectFileOrPasteToast, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003844), contentColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = compactButtonPadding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(
                        text = strings.checkAndImportButton,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    importSummaryToConfirm?.let { summary ->
        ImportConfirmationDialog(
            summary = summary,
            onConfirm = { replaceExisting ->
                viewModel.importFullBackup(summary.data, replaceExisting) { count ->
                    importSummaryToConfirm = null
                    Toast.makeText(context, strings.importedCountToast(count), Toast.LENGTH_LONG).show()
                }
            },
            onDismiss = { importSummaryToConfirm = null }
        )
    }
}
