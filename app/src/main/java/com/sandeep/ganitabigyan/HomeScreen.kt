// FILE: app/src/main/java/com/sandeep/ganitabigyan/HomeScreen.kt

package com.sandeep.ganitabigyan

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// --- DEFINE APP BAR HEIGHTS FOR ACCURATE CALCULATIONS ---
private val CollapsedAppBarHeight = 64.dp
private val ExpandedAppBarHeight = 152.dp

// --- UPDATED Data class to use String Resources ---
private data class HomeMenuItem(
    @StringRes val titleResId: Int,
    @StringRes val subtitleResId: Int?,
    val route: String,
    val gradient: List<Color>,
    val icon: ImageVector? = null,
    val textIcon: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateCheckResult?>(null) }
    val dataStore = remember { SettingsDataStore(context) }

    LaunchedEffect(Unit) {
        val result = UpdateChecker.checkForUpdates(context)
        if (result is UpdateCheckResult.UpdateAvailable) {
            updateResult = result
            showUpdateDialog = true
        }
    }

    if (showUpdateDialog && updateResult is UpdateCheckResult.UpdateAvailable) {
        // <<< FIX: Get the version name here, outside the Dialog Composable >>>
        val currentVersionName = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) { "1.0" }

        AutoUpdateDialog(
            result = updateResult as UpdateCheckResult.UpdateAvailable,
            // <<< FIX: Pass the version name as a parameter >>>
            currentVersionName = currentVersionName,
            onDismiss = { showUpdateDialog = false },
            onIgnoreVersion = { versionToIgnore ->
                scope.launch {
                    dataStore.setIgnoredUpdateVersion(versionToIgnore)
                    showUpdateDialog = false
                }
            },
            onUpdateNow = { downloadUrl, versionName ->
                if (canInstallUnknownApps(context)) {
                    startUpdateDownload(context, downloadUrl, versionName)
                } else {
                    Toast.makeText(context, R.string.permission_request_toast, Toast.LENGTH_LONG).show()
                    requestInstallPermission(context)
                }
                showUpdateDialog = false
            }
        )
    }

    val gradientOrange = listOf(Color(0xFFFFB74D), Color(0xFFFF9800))
    val gradientBlue = listOf(Color(0xFF64B5F6), Color(0xFF2196F3))
    val gradientPurple = listOf(Color(0xFFBA68C8), Color(0xFF9C27B0))
    val gradientRed = listOf(Color(0xFFE57373), Color(0xFFF44336))
    val gradientTeal = listOf(Color(0xFF4DB6AC), Color(0xFF009688))
    val gradientCyan = listOf(Color(0xFF4DD0E1), Color(0xFF00BCD4))

    val menuItems = listOf(
        HomeMenuItem(R.string.menu_start_game, R.string.menu_start_game_desc, AppDestinations.GAME_ROUTE, gradientOrange, Icons.Default.PlayArrow),
        HomeMenuItem(R.string.menu_progress, R.string.menu_progress_desc, AppDestinations.SCORE_HISTORY_ROUTE, gradientBlue, Icons.Default.Insights),
        HomeMenuItem(R.string.menu_panikia, R.string.menu_panikia_desc, AppDestinations.PANIKIA_LIST_ROUTE, gradientPurple, Icons.Default.MenuBook),
        HomeMenuItem(R.string.menu_numbers, R.string.menu_numbers_desc, AppDestinations.NUMBERS_ROUTE, gradientRed, textIcon = "୪୫"),
        HomeMenuItem(R.string.menu_drawing_pad, null, AppDestinations.DRAWING_ROUTE, gradientTeal, Icons.Default.Draw),
        HomeMenuItem(R.string.menu_calculator, null, AppDestinations.CALCULATOR_ROUTE, gradientCyan, Icons.Default.Calculate)
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = { /* Empty */ },
                    actions = {
                        IconButton(onClick = { navController.navigate(AppDestinations.SETTINGS_ROUTE) }) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_button_description))
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp).copy(alpha = 0.9f)
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color(0xFFF0F4F8)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(menuItems) { item ->
                    MenuItemCard(
                        item = item,
                        onClick = { navController.navigate(item.route) }
                    )
                }
            }
        }

        CollapsingToolbar(scrollBehavior = scrollBehavior)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingToolbar(scrollBehavior: TopAppBarScrollBehavior) {
    var titleSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val collapsedStartPadding = 16.dp
    val expandedHorizontalPadding = 24.dp

    val collapsedHeightPx = with(density) { CollapsedAppBarHeight.toPx() }
    val expandedHeightPx = with(density) { ExpandedAppBarHeight.toPx() }
    val collapsedStartPaddingPx = with(density) { collapsedStartPadding.toPx() }
    val expandedHorizontalPaddingPx = with(density) { expandedHorizontalPadding.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ExpandedAppBarHeight + topPadding)
            .padding(top = topPadding)
            .padding(horizontal = expandedHorizontalPadding),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            maxLines = 1,
            modifier = Modifier
                .onSizeChanged { if (titleSize != it) titleSize = it }
                .graphicsLayer {
                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    val startScale = 1.0f
                    val endScale = 0.7f
                    val currentScale = lerp(startScale, endScale, collapsedFraction)
                    val startTranslationX = (size.width / 2) - (titleSize.width / 2)
                    val endTranslationX = collapsedStartPaddingPx - expandedHorizontalPaddingPx
                    val currentTranslationX = lerp(startTranslationX, endTranslationX, collapsedFraction)
                    val startTranslationY = expandedHeightPx - titleSize.height
                    val endTranslationY = (collapsedHeightPx / 2) - (titleSize.height / 2)
                    val currentTranslationY = lerp(startTranslationY, endTranslationY, collapsedFraction)
                    scaleX = currentScale
                    scaleY = currentScale
                    translationX = currentTranslationX
                    translationY = currentTranslationY
                }
        )
    }
}


@Composable
private fun MenuItemCard(item: HomeMenuItem, onClick: () -> Unit) {
    val title = stringResource(id = item.titleResId)
    val subtitle = item.subtitleResId?.let { stringResource(id = it) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(brush = Brush.horizontalGradient(colors = item.gradient))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    fontSize = 26.sp
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 20.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = title,
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                } else if (item.textIcon != null) {
                    Text(
                        text = item.textIcon,
                        color = Color.White,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 1.em,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun AutoUpdateDialog(
    result: UpdateCheckResult.UpdateAvailable,
    // <<< FIX: Added new parameter >>>
    currentVersionName: String,
    onDismiss: () -> Unit,
    onIgnoreVersion: (String) -> Unit,
    onUpdateNow: (String, String) -> Unit
) {
    // <<< FIX: The try-catch is now removed from here >>>
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.update_dialog_title)) },
        text = { Text(stringResource(id = R.string.update_dialog_message, result.latestVersion, currentVersionName)) },
        confirmButton = {
            TextButton(
                onClick = { onUpdateNow(result.downloadUrl, result.latestVersion) }
            ) { Text(stringResource(id = R.string.update_dialog_update_now)) }
        },
        dismissButton = {
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.update_dialog_ignore_time))
                }
                TextButton(
                    onClick = { onIgnoreVersion(result.latestVersion) }
                ) {
                    Text(
                        stringResource(id = R.string.update_dialog_ignore_version),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

private fun canInstallUnknownApps(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.packageManager.canRequestPackageInstalls() else true
}

private fun requestInstallPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

private fun startUpdateDownload(context: Context, url: String, versionName: String) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(context.getString(R.string.download_notification_title, versionName))
            .setDescription(context.getString(R.string.download_notification_description))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Ganita-Bigyan-v$versionName.apk")
            .setMimeType("application/vnd.android.package-archive")
        downloadManager.enqueue(request)
        Toast.makeText(context, R.string.download_started, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.download_failed, e.message), Toast.LENGTH_LONG).show()
    }
}