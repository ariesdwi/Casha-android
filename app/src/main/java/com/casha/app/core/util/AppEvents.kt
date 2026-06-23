package com.casha.app.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Typed toast event emitted through [AppEvents]. */
sealed class ToastEvent(open val message: String) {
    data class Success(override val message: String) : ToastEvent(message)
    data class Error(override val message: String) : ToastEvent(message)
    data class Info(override val message: String) : ToastEvent(message)
    data class Warning(override val message: String) : ToastEvent(message)
}

/**
 * Global event bus for app-level UI events.
 * Emit from any ViewModel or anywhere in the app; collect in MainScreen.
 *
 * Usage:
 *   AppEvents.showSuccess("Wallet added")
 *   AppEvents.showError("Something went wrong")
 *   AppEvents.showInfo("Sync complete")
 *   AppEvents.showWarning("Low balance")
 */
object AppEvents {
    private val _toast = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 1)
    val toast = _toast.asSharedFlow()

    /** Backwards-compatible helper — shows an Info toast. */
    fun showSnackbar(message: String) = showInfo(message)

    fun showSuccess(message: String) { _toast.tryEmit(ToastEvent.Success(message)) }
    fun showError(message: String) { _toast.tryEmit(ToastEvent.Error(message)) }
    fun showInfo(message: String) { _toast.tryEmit(ToastEvent.Info(message)) }
    fun showWarning(message: String) { _toast.tryEmit(ToastEvent.Warning(message)) }
}
