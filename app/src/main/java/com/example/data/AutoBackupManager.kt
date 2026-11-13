package com.example.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AutoBackupStatus(
    val isEnabled: Boolean,
    val folderDisplayName: String,
    val hasCustomFolder: Boolean,
    val lastBackupEpochDay: Long,
    val lastBackupTimestampMs: Long,
    val lastBackupFileName: String,
    val lastStatusMessage: String
)

object AutoBackupManager {
    private const val PREFS_NAME = "playtime_auto_backup_prefs"
    private const val KEY_ENABLED = "auto_backup_enabled"
    private const val KEY_TREE_URI = "auto_backup_tree_uri"
    private const val KEY_FOLDER_NAME = "auto_backup_folder_name"
    private const val KEY_LAST_EPOCH_DAY = "last_auto_backup_epoch_day"
    private const val KEY_LAST_TIMESTAMP = "last_auto_backup_timestamp"
    private const val KEY_LAST_MESSAGE = "last_auto_backup_message"

    const val BACKUP_FILE_NAME = "game_media_backup.csv"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getStatus(context: Context): AutoBackupStatus {
        val prefs = getPrefs(context)
        val treeUriStr = prefs.getString(KEY_TREE_URI, null)
        val folderName = prefs.getString(KEY_FOLDER_NAME, null)
        val lastEpochDay = prefs.getLong(KEY_LAST_EPOCH_DAY, 0L)
        val lastTimestamp = prefs.getLong(KEY_LAST_TIMESTAMP, 0L)
        val lastMsg = prefs.getString(KEY_LAST_MESSAGE, "Not run yet") ?: "Not run yet"

        val displayName = when {
            !folderName.isNullOrBlank() -> folderName
            !treeUriStr.isNullOrBlank() -> "Selected Folder"
            else -> "Default Storage (App Backups)"
        }

        return AutoBackupStatus(
            isEnabled = prefs.getBoolean(KEY_ENABLED, true),
            folderDisplayName = displayName,
            hasCustomFolder = !treeUriStr.isNullOrBlank(),
            lastBackupEpochDay = lastEpochDay,
            lastBackupTimestampMs = lastTimestamp,
            lastBackupFileName = BACKUP_FILE_NAME,
            lastStatusMessage = lastMsg
        )
    }

    fun setCustomBackupFolder(context: Context, treeUri: Uri, folderDisplayName: String?) {
        try {
            // Persist URI permission so the app can access this directory across reboots
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(treeUri, flags)
        } catch (_: Exception) {
            // Handled safely
        }

        val name = folderDisplayName ?: treeUri.lastPathSegment ?: "Chosen Folder"
        getPrefs(context).edit()
            .putString(KEY_TREE_URI, treeUri.toString())
            .putString(KEY_FOLDER_NAME, name)
            .apply()
    }

    fun clearCustomBackupFolder(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_TREE_URI)
            .remove(KEY_FOLDER_NAME)
            .apply()
    }

    fun isAutoBackupEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, true)
    }

    fun setAutoBackupEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    /**
     * Checks if today's daily backup has already run.
     */
    fun shouldRunDailyBackup(context: Context): Boolean {
        val prefs = getPrefs(context)
        if (!prefs.getBoolean(KEY_ENABLED, true)) return false
        val todayEpoch = DailyDateHelper.getLocalEpochDay()
        val lastBackupEpoch = prefs.getLong(KEY_LAST_EPOCH_DAY, 0L)
        return todayEpoch > lastBackupEpoch
    }

    /**
     * Executes the daily auto-backup.
     * Ensures ONLY 1 backup file exists: deletes any existing file with this name before creating the new one.
     * Returns true if a new backup was created.
     */
    suspend fun runDailyBackup(
        context: Context,
        jsonContent: String,
        force: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val prefs = getPrefs(context)
        if (!force && !prefs.getBoolean(KEY_ENABLED, true)) {
            return@withContext Result.failure(IllegalStateException("Auto-backup disabled"))
        }
        val todayEpoch = DailyDateHelper.getLocalEpochDay()
        val lastBackupEpoch = prefs.getLong(KEY_LAST_EPOCH_DAY, 0L)

        if (!force && todayEpoch <= lastBackupEpoch) {
            return@withContext Result.success("Already backed up for today")
        }

        val treeUriStr = prefs.getString(KEY_TREE_URI, null)

        // Try writing to user-selected folder first if configured
        if (!treeUriStr.isNullOrBlank()) {
            try {
                val treeUri = Uri.parse(treeUriStr)
                val targetFileUri = writeToTreeUri(context, treeUri, BACKUP_FILE_NAME, jsonContent)
                if (targetFileUri != null) {
                    recordSuccess(context, todayEpoch, "Saved to $BACKUP_FILE_NAME")
                    return@withContext Result.success("Backup saved to ${prefs.getString(KEY_FOLDER_NAME, "custom folder")}/$BACKUP_FILE_NAME")
                }
            } catch (e: Exception) {
                // If custom folder fails (e.g. SD card removed or permissions expired), fall back to default storage
            }
        }

        // Fallback or default location: App external files dir "backups"
        try {
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val targetFile = File(backupDir, BACKUP_FILE_NAME)
            // Ensure only 1 backup file exists: delete old backup file if present
            if (targetFile.exists()) {
                targetFile.delete()
            }

            FileOutputStream(targetFile).use { out ->
                out.write(jsonContent.toByteArray(Charsets.UTF_8))
            }

            recordSuccess(context, todayEpoch, "Saved to default storage ($BACKUP_FILE_NAME)")
            Result.success("Backup saved to ${targetFile.name}")
        } catch (e: Exception) {
            recordFailure(context, "Error: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    private fun writeToTreeUri(
        context: Context,
        treeUri: Uri,
        fileName: String,
        content: String
    ): Uri? {
        val contentResolver = context.contentResolver
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)

        // Query children to find existing file and delete it (ensures ONLY 1 backup file exists)
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                childrenUri,
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )
            while (cursor != null && cursor.moveToNext()) {
                val name = cursor.getString(1)
                if (name.equals(fileName, ignoreCase = true)) {
                    val existingDocId = cursor.getString(0)
                    val existingDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, existingDocId)
                    try {
                        DocumentsContract.deleteDocument(contentResolver, existingDocUri)
                    } catch (_: Exception) {
                        // If delete not supported, proceed to create or overwrite
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore query errors
        } finally {
            cursor?.close()
        }

        // Create new document in the folder
        val newDocUri = DocumentsContract.createDocument(
            contentResolver,
            parentDocUri,
            "text/csv",
            fileName
        ) ?: return null

        contentResolver.openOutputStream(newDocUri, "wt")?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        }

        return newDocUri
    }

    private fun recordSuccess(context: Context, todayEpoch: Long, message: String) {
        val now = System.currentTimeMillis()
        getPrefs(context).edit()
            .putLong(KEY_LAST_EPOCH_DAY, todayEpoch)
            .putLong(KEY_LAST_TIMESTAMP, now)
            .putString(KEY_LAST_MESSAGE, message)
            .apply()
    }

    private fun recordFailure(context: Context, error: String) {
        getPrefs(context).edit()
            .putString(KEY_LAST_MESSAGE, error)
            .apply()
    }
}
