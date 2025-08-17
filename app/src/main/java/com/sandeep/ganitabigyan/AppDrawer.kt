package com.sandeep.ganitabigyan

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawerContent(
    drawerState: DrawerState,
    scope: CoroutineScope,
    isAutoScrollEnabled: Boolean,
    onAutoScrollToggled: (Boolean) -> Unit,
    onNavigateToGame: () -> Unit, // <--- 1. ନୂଆ ପାରାମିଟର ଯୋଡ଼ାଗଲା
    onTimedChallengeClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToScore: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToPanikia: () -> Unit,
    onNavigateToNumbers: () -> Unit
) {
    val context = LocalContext.current
    val packageInfo = try { context.packageManager.getPackageInfo(context.packageName, 0) } catch (e: Exception) { null }
    val versionName = packageInfo?.versionName ?: "1.0"

    ModalDrawerSheet {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "ଗଣିତ ବିଜ୍ଞ",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            HorizontalDivider()

            // v-- 2. ଏହା ହେଉଛି ନୂଆ ମେନୁ ଆଇଟମ୍ --v
            NavigationDrawerItem(
                label = { Text("ଖେଳ ପୃଷ୍ଟା") },
                selected = false,
                icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Game Screen") },
                onClick = {
                    onNavigateToGame() // ନୂଆ ଫଙ୍କସନକୁ କଲ୍ କରାଗଲା
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            // ^-- ନୂଆ ମେନୁ ଆଇଟମ୍ ଏଠାରେ ଶେଷ ହେଲା --^

            NavigationDrawerItem(
                label = { Text("ସମୟ ଚ୍ୟାଲେଞ୍ଜ") },
                selected = false,
                icon = { Icon(Icons.Default.Timer, contentDescription = "Timed Challenge") },
                onClick = {
                    Toast.makeText(context, "ଏହି ସୁବିଧା ଏବେ ଉପଲବ୍ଧ ନାହିଁ", Toast.LENGTH_SHORT).show()
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("ଅଟୋ ସ୍କ୍ରୋଲ୍") },
                selected = false,
                icon = { Icon(Icons.Default.SwapVert, contentDescription = "Auto Scroll") },
                badge = {
                    Switch(checked = isAutoScrollEnabled, onCheckedChange = onAutoScrollToggled)
                },
                onClick = { onAutoScrollToggled(!isAutoScrollEnabled) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("ପ୍ରଶ୍ନ ଇତିହାସ") },
                selected = false,
                icon = { Icon(Icons.Default.History, contentDescription = "History") },
                onClick = {
                    onNavigateToHistory()
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("କେତେ ଠିକ୍ କେତେ ଭୁଲ୍") },
                selected = false,
                icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Score") },
                onClick = {
                    onNavigateToScore()
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("ପଣିକିଆ") },
                selected = false,
                icon = { Icon(Icons.Default.MenuBook, contentDescription = "Panikia") },
                onClick = {
                    onNavigateToPanikia()
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("ସଙ୍ଖ୍ୟା") },
                selected = false,
                icon = { Icon(Icons.Default.Dialpad, contentDescription = "Numbers") },
                onClick = {
                    onNavigateToNumbers()
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("କ୍ୟାଲକୁଲେଟର") },
                selected = false,
                icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                onClick = {
                    onNavigateToCalculator()
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            NavigationDrawerItem(
                label = { Text("ଆପ୍ ବିଷୟରେ") },
                selected = false,
                icon = { Icon(Icons.Default.Info, contentDescription = "About App") },
                onClick = {
                    onNavigateToAbout()
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )


            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "ସଂସ୍କରଣ ${versionName}\nସନ୍ଦୀପ୍ ବିଶ୍ବାଳ ଜି'ଙ୍କ ଦ୍ୱାରା ନିର୍ମିତ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}