package com.sandeep.ganitabigyan

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// ଅପଡେଟ୍ ଯାଞ୍ଚର ଫଳାଫଳ ପାଇଁ sealed class, ଏଥିରେ ଏବେ ଡାଉନଲୋଡ୍ URL ମଧ୍ୟ ରହିବ
sealed class UpdateCheckResult {
    data class UpdateAvailable(val latestVersion: String, val downloadUrl: String) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    object Error : UpdateCheckResult()
}

/**
 * ଏହି ଫଙ୍କସନ୍ ଯାଞ୍ଚ କରେ ଯେ ଆପ୍ ଅଜଣା ସୋର୍ସରୁ ଇନଷ୍ଟଲ୍ କରିବାକୁ ଅନୁମତି ପାଇଛି କି ନାହିଁ।
 */
private fun canInstallUnknownApps(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.packageManager.canRequestPackageInstalls()
    } else {
        true // ପୁରୁଣା ଆଣ୍ଡ୍ରଏଡ୍ ଭର୍ସନରେ ଏହି ଅନୁମତି ଆବଶ୍ୟକ ନୁହେଁ
    }
}

/**
 * ଏହି ଫଙ୍କସନ୍ ବ୍ୟବହାରକାରୀଙ୍କୁ "Install unknown apps" ଅନୁମତି ଦେବା ପାଇଁ ସେଟିଙ୍ଗସକୁ ନେଇଯାଏ।
 */
private fun requestInstallPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

/**
 * ଏହି ଫଙ୍କସନ୍ DownloadManager ବ୍ୟବହାର କରି APK ଫାଇଲ୍ ଡାଉନଲୋଡ୍ ଆରମ୍ଭ କରେ।
 */
private fun startUpdateDownload(context: Context, url: String, versionName: String) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("ଗଣିତ ବିଜ୍ଞ v$versionName ଡାଉନଲୋଡ୍ ହେଉଛି")
            .setDescription("ନୂଆ ଅପଡେଟ୍ ଡାଉନଲୋଡ୍ କରାଯାଉଛି...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Ganita-Bigyan-v$versionName.apk")
            .setMimeType("application/vnd.android.package-archive")

        downloadManager.enqueue(request)
        Toast.makeText(context, "ଡାଉନଲୋଡ୍ ଆରମ୍ଭ ହେଉଛି...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "ଡାଉନଲୋଡ୍ ବିଫଳ ହେଲା: ${e.message}", Toast.LENGTH_LONG).show()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: Exception) {
        null
    }
    val currentVersionName = packageInfo?.versionName ?: "1.0"
    val scope = rememberCoroutineScope()

    var isCheckingForUpdate by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateCheckResult?>(null) }
    var showDialog by remember { mutableStateOf(false) }


    fun checkForUpdates() {
        isCheckingForUpdate = true
        scope.launch(Dispatchers.IO) {
            val result = try {
                val url = URL("https://api.github.com/repos/imsbg/Ganita-Bingya-App/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val latestVersion = json.getString("tag_name").removePrefix("v")

                // ନୂଆ: GitHub ରିଲିଜରୁ ସିଧାସଳଖ .apk ଡାଉନଲୋଡ୍ URL ବାହାର କରନ୍ତୁ
                var apkUrl = ""
                val assets = json.getJSONArray("assets")
                if (assets.length() > 0) {
                    val firstAsset = assets.getJSONObject(0)
                    apkUrl = firstAsset.getString("browser_download_url")
                }

                if (latestVersion > currentVersionName && apkUrl.isNotEmpty()) {
                    UpdateCheckResult.UpdateAvailable(latestVersion, apkUrl)
                } else {
                    UpdateCheckResult.UpToDate
                }
            } catch (e: Exception) {
                UpdateCheckResult.Error
            }

            withContext(Dispatchers.Main) {
                updateResult = result
                isCheckingForUpdate = false
                showDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ଆପ୍ ବିଷୟରେ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ଗଣିତ ବିଜ୍ଞ",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ସଂସ୍କରଣ $currentVersionName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ସନ୍ଦୀପ୍ ବିଶ୍ବାଳ ଜି'ଙ୍କ ଦ୍ୱାରା ନିର୍ମିତ",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            InfoRow(
                icon = Icons.Default.SystemUpdate,
                text = "ଅପଡେଟ୍ ଯାଞ୍ଚ କରନ୍ତୁ",
                isChecking = isCheckingForUpdate,
                onClick = {
                    if (!isCheckingForUpdate) {
                        checkForUpdates()
                    }
                }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoRow(
                icon = Icons.Default.Person,
                text = "ମୋତେ ଅନୁସରଣ କରନ୍ତୁ",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/sandeepbiswalg"))
                    context.startActivity(intent)
                }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoRow(
                icon = Icons.Default.Language,
                text = "ଅନଲାଇନ୍ ଖେଳନ୍ତୁ",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://odiagames.netlify.app/?lang=odia"))
                    context.startActivity(intent)
                }
            )
        }
    }

    // ଏହି ବ୍ଲକ୍ ଡାଉନଲୋଡ୍ ଏବଂ ଅନୁମତି ପାଇଁ ଡାୟଲଗ୍ ଦେଖାଏ
    if (showDialog) {
        when (val result = updateResult) {
            is UpdateCheckResult.UpdateAvailable -> {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("ନୂଆ ଅପଡେଟ୍ ଉପଲବ୍ଧ") },
                    text = { Text("ଏକ ନୂଆ ସଂସ୍କରଣ (v${result.latestVersion}) ଉପଲବ୍ଧ ଅଛି। ଆପଣ ବର୍ତ୍ତମାନ (v$currentVersionName) ବ୍ୟବହାର କରୁଛନ୍ତି।") },
                    confirmButton = {
                        TextButton(onClick = {
                            if (canInstallUnknownApps(context)) {
                                // ଅନୁମତି ମିଳିସାରିଛି, ଡାଉନଲୋଡ୍ ଆରମ୍ଭ କରନ୍ତୁ
                                startUpdateDownload(context, result.downloadUrl, result.latestVersion)
                            } else {
                                // ଅନୁମତି ମିଳିନାହିଁ, ବ୍ୟବହାରକାରୀଙ୍କୁ ଅନୁମତି ମାଗନ୍ତୁ
                                Toast.makeText(context, "ଏହି ସ୍ରୋତରୁ ଆପ୍ ଇନଷ୍ଟଲ୍ କରିବାକୁ ଦୟାକରି ଅନୁମତି ଦିଅନ୍ତୁ।", Toast.LENGTH_LONG).show()
                                requestInstallPermission(context)
                            }
                            showDialog = false
                        }) {
                            Text("ଡାଉନଲୋଡ୍ କରନ୍ତୁ")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("ପରେ କେବେ")
                        }
                    }
                )
            }
            UpdateCheckResult.UpToDate -> {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("ଆପ୍ ଅପ-ଟୁ-ଡେଟ୍ ଅଛି") },
                    text = { Text("ଆପଣ ଗଣିତ ବିଜ୍ଞର ସର୍ବଶେଷ ସଂସ୍କରଣ ବ୍ୟବହାର କରୁଛନ୍ତି ।") },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("ଠିକ୍ ଅଛି")
                        }
                    }
                )
            }
            UpdateCheckResult.Error -> {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("ତ୍ରୁଟି") },
                    text = { Text("ଅପଡେଟ୍ ଯାଞ୍ଚ କରିବାରେ ଅସମର୍ଥ, ଦୟାକରି ଆପଣଙ୍କର ଇଣ୍ଟରନେଟ୍ ସଂଯୋଗ ଯାଞ୍ଚ କରନ୍ତୁ") },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("ଠିକ୍ ଅଛି")
                        }
                    }
                )
            }
            null -> { /* କିଛି କରନ୍ତୁ ନାହିଁ */ }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String, isChecking: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (isChecking) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}