package com.casha.app.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global event bus for app-level UI events (e.g. showing a snackbar above the tab bar).
 * Emit from any ViewModel, collect in the root Scaffold (MainScreen).
 */
object AppEvents {
    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbar = _snackbar.asSharedFlow()

    fun showSnackbar(message: String) {
        _snackbar.tryEmit(message)
    }
}
