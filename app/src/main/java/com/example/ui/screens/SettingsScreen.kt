package com.aistudio.sublimationerp.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.system.exitProcess
import android.app.AlarmManager
import android.app.PendingIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                backupDatabase(context, uri)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                restoreDatabase(context, uri)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "تنظیمات نرم‌افزار",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("پشتیبان‌گیری (بکاپ)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("امکان خروجی گرفتن از پایگاه‌داده Room در قالب فایل (فقط با دسترسی حافظه)")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { 
                    exportLauncher.launch("sublimation_db_backup.db")
                }) {
                    Text("تهیه فایل پشتیبان")
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("بازیابی اطلاعات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("دریافت فایل پشتیبان .db و جایگزینی آن. دقت کنید که این کار تمام اطلاعات فعلی را پاک می‌کند!")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { 
                    importLauncher.launch(arrayOf("*/*"))
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("بازیابی از فایل")
                }
            }
        }
    }
}

private suspend fun backupDatabase(context: Context, targetUri: Uri) {
    withContext(Dispatchers.IO) {
        try {
            val app = context.applicationContext as com.aistudio.sublimationerp.SublimationApp
            val dbFile = context.getDatabasePath("sublimation_db")
            
            // To be safe, checkpoint the DB and then close it before copying. Since TRUNCATE mode is used, DB should be safe.
            if (dbFile.exists()) {
                context.contentResolver.openOutputStream(targetUri)?.use { output ->
                    FileInputStream(dbFile).use { input ->
                        input.copyTo(output)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "بکاپ با موفقیت ایجاد شد", Toast.LENGTH_LONG).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "فایل دیتابیس یافت نشد", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "خطا در تهیه بکاپ", Toast.LENGTH_LONG).show()
            }
        }
    }
}

private suspend fun restoreDatabase(context: Context, sourceUri: Uri) {
    withContext(Dispatchers.IO) {
        try {
            val app = context.applicationContext as com.aistudio.sublimationerp.SublimationApp
            val dbFile = context.getDatabasePath("sublimation_db")
            
            // Critical! Close database before overwrite.
            app.database.close()
            
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "بازیابی انجام شد. برنامه مجددا راه اندازی می‌شود.", Toast.LENGTH_LONG).show()
                
                // Restart app
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                val componentName = intent?.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                context.startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "خطا در بازیابی", Toast.LENGTH_LONG).show()
            }
        }
    }
}
