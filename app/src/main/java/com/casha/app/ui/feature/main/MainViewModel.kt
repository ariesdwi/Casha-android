package com.casha.app.ui.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.core.notification.NotificationManager
import com.casha.app.domain.model.NotificationCasha
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _notificationEvents = MutableSharedFlow<NotificationCasha>()
    val notificationEvents = _notificationEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            notificationManager.notifications.collect { notification ->
                // Basic filtering: only show if title is present (indicating it's a real user alerts)
                if (notification.title.isNotEmpty()) {
                    _notificationEvents.emit(notification)
                }
            }
        }
    }
}
