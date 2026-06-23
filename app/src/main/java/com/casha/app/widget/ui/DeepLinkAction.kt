package com.casha.app.widget.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/**
 * Shared action callback for deep link navigation from widgets.
 */
class DeepLinkAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val deepLink = parameters[DeepLinkKey] ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
            setClassName(context.packageName, "com.casha.app.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }

    companion object {
        val DeepLinkKey = ActionParameters.Key<String>("deep_link")
    }
}
