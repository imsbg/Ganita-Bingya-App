// FILE: app/src/main/java/com/sandeep/ganitabigyan/AboutScreen.kt
// VERSION: FINAL - Swaps the GitHub and Telegram icons back.

package com.sandeep.ganitabigyan

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// Data models are simplified as changelog logic is moved
data class Contributor(val name: String, val role: String, val imageResId: Int, val profileUrl: String)
sealed class UpdateCheckResult {
    data class UpdateAvailable(val latestVersion: String, val downloadUrl: String) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult(); object Error : UpdateCheckResult()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val currentVersionName = try { context.packageManager.getPackageInfo(context.packageName, 0).versionName } catch (e: Exception) { "1.0" }
    val scope = rememberCoroutineScope()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateCheckResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_screen_title)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { ContributorsCard() }
            item { SupportCard() }
            item { SocialCard() }
            item { OtherCard(
                versionName = currentVersionName,
                onCheckForUpdate = { checkForUpdates(scope, currentVersionName) { result -> updateResult = result; showUpdateDialog = true } },
                onShowChangelog = { navController.navigate(AppDestinations.CHANGELOG_ROUTE) }
            )}
            item { Footer() }
        }
    }

    if (showUpdateDialog) { UpdateDialog(updateResult = updateResult, currentVersionName = currentVersionName, onDismiss = { showUpdateDialog = false }) }
}


// --- REUSABLE UI COMPONENTS ---
@Composable
private fun AboutCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

// --- UPDATED CARDS ---
@Composable
private fun SupportCard() {
    val context = LocalContext.current
    AboutCard(title = stringResource(R.string.support_development)) {
        // <<< FIX: Added the GitHub icon here >>>
        InfoRow(painterResource(id = R.drawable.ic_github), stringResource(R.string.support_github), stringResource(R.string.support_github_desc)) { openUrl(context, "https://github.com/imsbg/Ganita-Bingya-App") }
        InfoRow(Icons.Default.Translate, stringResource(R.string.support_translate), stringResource(R.string.support_translate_desc)) { openUrl(context, "https://github.com/imsbg/Ganita-Bingya-App#contributing") }
        InfoRow(Icons.Default.VolunteerActivism, stringResource(R.string.support_donate), stringResource(R.string.support_donate_desc)) { openUrl(context, "https://imsbg.github.io/Ganita-Bingya-App/donate") }
        InfoRow(Icons.Default.BugReport, stringResource(R.string.support_report_bug), stringResource(R.string.support_report_bug_desc)) { openUrl(context, "https://github.com/imsbg/ganita-bingya-app/issues") }
        InfoRow(Icons.Default.Share, stringResource(R.string.support_share), stringResource(R.string.support_share_desc)) {
            val shareText = context.getString(R.string.share_message)
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }
            context.startActivity(Intent.createChooser(intent, null))
        }
    }
}

@Composable
private fun SocialCard() {
    val context = LocalContext.current
    AboutCard(title = stringResource(R.string.social)) {
        InfoRow(Icons.Default.Language, stringResource(R.string.social_website), stringResource(R.string.social_website_desc)) { openUrl(context, "https://imsbg.github.io/Ganita-Bingya-App/") }
        InfoRow(painterResource(id = R.drawable.ic_instagram), stringResource(R.string.social_instagram), stringResource(R.string.social_instagram_desc)) { openUrl(context, "https://www.instagram.com/sandeepbiswalg/") }
        InfoRow(painterResource(id = R.drawable.ic_twitter_x), stringResource(R.string.social_twitter), stringResource(R.string.social_twitter_desc)) { openUrl(context, "https://x.com/SandeepBiswalG") }
        // <<< FIX: Changed back to the Telegram icon and details >>>
        InfoRow(Icons.Default.Send, stringResource(R.string.social_telegram), stringResource(R.string.social_telegram_desc)) { openUrl(context, "https://t.me/sbgapps") }
    }
}


// ... All other Composables and Helper Functions are unchanged ...
@Composable private fun InfoRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) { InfoRowContent(icon = { Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }, title = title, subtitle = subtitle, onClick = onClick) }
@Composable private fun InfoRow(icon: Painter, title: String, subtitle: String, onClick: () -> Unit) { InfoRowContent(icon = { Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) }, title = title, subtitle = subtitle, onClick = onClick) }
@Composable private fun InfoRowContent(icon: @Composable () -> Unit, title: String, subtitle: String, onClick: () -> Unit) { Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { icon(); Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(text = title, style = MaterialTheme.typography.bodyLarge); Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun ContributorRow(contributor: Contributor) { val context = LocalContext.current; Row(modifier = Modifier.fillMaxWidth().clickable { openUrl(context, contributor.profileUrl) }.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Image(painter = painterResource(id = contributor.imageResId), contentDescription = contributor.name, modifier = Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop); Spacer(modifier = Modifier.width(16.dp)); Column { Text(text = contributor.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold); Text(text = contributor.role, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun ContributorsCard() { val contributors = listOf(Contributor(stringResource(R.string.akash_t_name), stringResource(R.string.akash_t_role), R.drawable.akash_t, "https://www.instagram.com/akash_thirugnanam/")); var isExpanded by remember { mutableStateOf(false) }; AboutCard(title = stringResource(R.string.members_and_contributors)) { ContributorRow(Contributor(name = stringResource(R.string.sandeep_biswal_name), role = stringResource(R.string.sandeep_biswal_role), imageResId = R.drawable.sandeep_biswal, profileUrl = "https://www.instagram.com/sandeepbiswalg/")); AnimatedVisibility(visible = isExpanded) { Column { contributors.forEach { ContributorRow(it) } } }; if (contributors.isNotEmpty()) { TextButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.align(Alignment.End)) { Text(if (isExpanded) stringResource(R.string.show_less) else stringResource(R.string.show_more)) } } } }
@Composable private fun OtherCard(versionName: String, onCheckForUpdate: () -> Unit, onShowChangelog: () -> Unit) { AboutCard(title = stringResource(R.string.other)) { InfoRow(Icons.Default.History, stringResource(R.string.other_changelog), stringResource(R.string.other_changelog_desc), onShowChangelog); InfoRow(Icons.Default.SystemUpdate, stringResource(R.string.other_check_for_updates), "v$versionName", onCheckForUpdate) } }
@Composable private fun Footer() { Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) { Text(text = stringResource(R.string.made_with_love), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable fun UpdateDialog(updateResult: UpdateCheckResult?, currentVersionName: String, onDismiss: () -> Unit) { val context = LocalContext.current; when (updateResult) { is UpdateCheckResult.UpdateAvailable -> { AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.update_available_title)) }, text = { Text(stringResource(R.string.update_available_message, updateResult.latestVersion, currentVersionName)) }, confirmButton = { TextButton(onClick = { if (canInstallUnknownApps(context)) { startUpdateDownload(context, updateResult.downloadUrl, updateResult.latestVersion) } else { Toast.makeText(context, R.string.permission_request_toast, Toast.LENGTH_LONG).show(); requestInstallPermission(context) }; onDismiss() }) { Text(stringResource(R.string.download_button)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.later_button)) } }) }; UpdateCheckResult.UpToDate -> { AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.up_to_date_title)) }, text = { Text(stringResource(R.string.up_to_date_message)) }, confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok_button)) } }) }; UpdateCheckResult.Error -> { AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.error_title)) }, text = { Text(stringResource(R.string.error_check_connection)) }, confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok_button)) } }) }; null -> {} } }
private fun openUrl(context: Context, url: String) { val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)); context.startActivity(intent) }
private fun checkForUpdates(scope: CoroutineScope, currentVersionName: String, onResult: (UpdateCheckResult) -> Unit) { scope.launch(Dispatchers.IO) { val result = try { val url = URL("https://api.github.com/repos/imsbg/Ganita-Bingya-App/releases/latest"); val connection = url.openConnection() as java.net.HttpURLConnection; val response = connection.inputStream.bufferedReader().readText(); val json = JSONObject(response); val latestVersion = json.getString("tag_name").removePrefix("v"); var apkUrl = ""; val assets = json.getJSONArray("assets"); if (assets.length() > 0) { apkUrl = assets.getJSONObject(0).getString("browser_download_url") }; if (latestVersion > currentVersionName && apkUrl.isNotEmpty()) { UpdateCheckResult.UpdateAvailable(latestVersion, apkUrl) } else { UpdateCheckResult.UpToDate } } catch (e: Exception) { UpdateCheckResult.Error }; withContext(Dispatchers.Main) { onResult(result) } } }
private fun canInstallUnknownApps(context: Context): Boolean { return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.packageManager.canRequestPackageInstalls() else true }
private fun requestInstallPermission(context: Context) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply { data = Uri.parse("package:${context.packageName}") }; context.startActivity(intent) } }
private fun startUpdateDownload(context: Context, url: String, versionName: String) { try { val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager; val request = DownloadManager.Request(Uri.parse(url)).setTitle(context.getString(R.string.download_notification_title, versionName)).setDescription(context.getString(R.string.download_notification_description)).setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Ganita-Bigyan-v$versionName.apk").setMimeType("application/vnd.android.package-archive"); downloadManager.enqueue(request); Toast.makeText(context, R.string.download_started, Toast.LENGTH_SHORT).show() } catch (e: Exception) { Toast.makeText(context, context.getString(R.string.download_failed, e.message), Toast.LENGTH_LONG).show() } }