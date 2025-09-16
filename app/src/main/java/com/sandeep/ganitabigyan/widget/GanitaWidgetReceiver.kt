package com.sandeep.ganitabigyan.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.sandeep.ganitabigyan.MainActivity
import com.sandeep.ganitabigyan.R

// We define unique names for each button's action.
// This is important to make sure Android treats each button tap as a separate event.
private const val ACTION_NAVIGATE_GAME = "com.sandeep.ganitabigyan.widget.ACTION_NAVIGATE_GAME"
private const val ACTION_NAVIGATE_PANIKIA = "com.sandeep.ganitabigyan.widget.ACTION_NAVIGATE_PANIKIA"
private const val ACTION_NAVIGATE_NUMBERS = "com.sandeep.ganitabigyan.widget.ACTION_NAVIGATE_NUMBERS"
private const val ACTION_NAVIGATE_DRAWING = "com.sandeep.ganitabigyan.widget.ACTION_NAVIGATE_DRAWING"


class GanitaWidgetReceiver : AppWidgetProvider() {

    /**
     * This function is called when the widget is first placed on the home screen,
     * and also when it needs to be updated.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // A user can have more than one of your widgets, so we loop through all of them.
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Get the layout for the widget
        val views = RemoteViews(context.packageName, R.layout.ganita_widget_layout)

        // --- Link each button in the layout to a specific action ---

        // Link the "Start Game" button
        views.setOnClickPendingIntent(
            R.id.widget_button_game,
            createNavPendingIntent(context, "game", ACTION_NAVIGATE_GAME)
        )

        // Link the "Panikia" (Multiplication) button
        views.setOnClickPendingIntent(
            R.id.widget_button_panikia,
            createNavPendingIntent(context, "panikia_list", ACTION_NAVIGATE_PANIKIA)
        )

        // Link the "Numbers" button
        views.setOnClickPendingIntent(
            R.id.widget_button_numbers,
            createNavPendingIntent(context, "numbers", ACTION_NAVIGATE_NUMBERS)
        )

        // Link the "Drawing Pad" button
        views.setOnClickPendingIntent(
            R.id.widget_button_drawing,
            createNavPendingIntent(context, "drawing", ACTION_NAVIGATE_DRAWING)
        )

        // Tell the AppWidgetManager to apply the changes to the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * A helper function to create the "PendingIntent" that launches our app to a specific screen.
     */
    private fun createNavPendingIntent(context: Context, route: String, action: String): PendingIntent {
        // Create an intent that points to our app's MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            // Set the unique action
            this.action = action
            // Add the destination screen's name (the "route") as extra data
            putExtra(WIDGET_DESTINATION_KEY, route)
            // Flags to make sure the app opens correctly
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Wrap the intent in a PendingIntent. This allows the home screen to execute our intent.
        return PendingIntent.getActivity(
            context,
            0, // requestCode is 0 because our `action` string makes the intent unique
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        // This is the key we use to send and receive the destination screen's name.
        const val WIDGET_DESTINATION_KEY = "widget_destination_route"
    }
}