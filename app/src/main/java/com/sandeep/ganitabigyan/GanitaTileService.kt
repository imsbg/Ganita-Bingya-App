// FILE: app/src/main/java/com/sandeep/ganitabigyan/GanitaTileService.kt

package com.sandeep.ganitabigyan

import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class GanitaTileService : TileService() {

    // A tag for our logs, so we can easily filter them in Logcat.
    private val TAG = "GanitaTileService"

    /**
     * Called when the user adds the tile to their Quick Settings panel.
     */
    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "Tile was added by the user.")
    }

    /**
     * Called when the system wants to bind to this service.
     * This happens when the tile becomes visible to the user.
     */
    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "onStartListening called. The system is trying to activate the tile.")

        val tile = qsTile ?: return

        // Update the tile to its active state
        tile.state = Tile.STATE_INACTIVE
        tile.label = getString(R.string.app_name)
        tile.subtitle = getString(R.string.qs_tile_subtitle)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_tile_logo)
        tile.updateTile()

        Log.d(TAG, "Tile state set to ACTIVE and updated.")
    }

    /**
     * Called when the user taps on your tile.
     */
    override fun onClick() {
        super.onClick()
        Log.d(TAG, "onClick called. Attempting to start the app.")

        // Intent to open the app using a deep link
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("ganitabingya://open"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivityAndCollapse(intent)
            Log.d(TAG, "startActivityAndCollapse successful.")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting activity. Is the deep link intent-filter missing in AndroidManifest.xml?", e)
        }
    }

    /**
     * Called when the tile is no longer visible to the user.
     */
    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "onStopListening called.")
    }

    /**
     * Called when the user removes the tile from their Quick Settings panel.
     */
    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(TAG, "Tile was removed by the user.")
    }
}