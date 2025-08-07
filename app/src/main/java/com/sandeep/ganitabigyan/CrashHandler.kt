package com.sandeep.ganitabigyan

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sandeep.ganitabigyan.ui.theme.GanitaBigyanTheme
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class GanitaBigyanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
        scheduleMathReminders()
    }

    private fun setupCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val intent = Intent(this, CrashActivity::class.java).apply {
                putExtra("error_details", throwable.stackTraceToString())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    private fun scheduleMathReminders() {
        val reminderWorkRequest = PeriodicWorkRequestBuilder<MathReminderWorker>(12, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "math_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWorkRequest
        )
    }
}

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val errorDetails = intent.getStringExtra("error_details") ?: "No error details available."
        setContent {
            GanitaBigyanTheme {
                CrashScreen(errorDetails = errorDetails)
            }
        }
    }
}

@Composable
fun CrashScreen(errorDetails: String) {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ଗଣିତ ବିଜ୍ଞ",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ଏକ ଅସୁବିଧା ଦେଖାଦେଇଛି",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ଆପ୍ କ୍ରାଶ୍ ହୋଇଯାଇଛି। ଆପଣ ଏହି ତ୍ରୁଟି ରିପୋର୍ଟକୁ ଡେଭଲପର୍‌ଙ୍କୁ ପଠାଇପାରିବେ।",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = errorDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Crash Report", errorDetails)
                    clipboard.setPrimaryClip(clip)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ରିପୋର୍ଟ କପି କରନ୍ତୁ")
            }
            TextButton(
                onClick = {
                    (context as? Activity)?.finish()
                    Process.killProcess(Process.myPid())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ଆପ୍ ବନ୍ଦ କରନ୍ତୁ")
            }
        }
    }
}