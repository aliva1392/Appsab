package com.aistudio.sublimationerp.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aistudio.sublimationerp.data.db.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) {
            backupDatabase(context, uri)
        }
    }
    
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            restoreDatabase(context, uri)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        
        Button(
            onClick = { backupLauncher.launch("sublimation_backup.db") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("تهیه فایل پشتیبان")
        }
        
        Button(
            onClick = { restoreLauncher.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("بازیابی از فایل")
        }
    }
}

fun backupDatabase(context: Context, uri: Uri) {
    try {
        // Force WAL checkpoint before backup
        val db = AppDatabase.getDatabase(context)
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

        val dbFile = context.getDatabasePath("sublimation_database")
        if (dbFile.exists()) {
            FileInputStream(dbFile).use { input ->
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    input.copyTo(output)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun restoreDatabase(context: Context, uri: Uri) {
    try {
        // Close Room connection first
        AppDatabase.closeDatabase()

        val dbFile = context.getDatabasePath("sublimation_database")
        // همچنین WAL files را هم پاک کن
        File(dbFile.path + "-shm").delete()
        File(dbFile.path + "-wal").delete()

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
        // Restart the app to reinitialize Room with new database
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent != null) {
            context.startActivity(intent)
        }
        android.os.Process.killProcess(android.os.Process.myPid())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

