package com.casha.app.ui.feature.profile

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.os.LocaleListCompat

data class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String
)

val supportedLanguages = listOf(
    AppLanguage("en",    "English",             "English",            "🇺🇸"),
    AppLanguage("ar",    "Arabic",              "العربية",            "🇸🇦"),
    AppLanguage("de",    "German",              "Deutsch",            "🇩🇪"),
    AppLanguage("es",    "Spanish",             "Español",            "🇪🇸"),
    AppLanguage("fr",    "French",              "Français",           "🇫🇷"),
    AppLanguage("hi",    "Hindi",               "हिन्दी",              "🇮🇳"),
    AppLanguage("id",    "Indonesian",          "Bahasa Indonesia",   "🇮🇩"),
    AppLanguage("ja",    "Japanese",            "日本語",              "🇯🇵"),
    AppLanguage("ko",    "Korean",              "한국어",              "🇰🇷"),
    AppLanguage("pt",    "Portuguese",          "Português",          "🇵🇹"),
    AppLanguage("pt-BR", "Portuguese (Brazil)", "Português (Brasil)", "🇧🇷"),
    AppLanguage("zh",    "Chinese (Simplified)","中文（简体）",         "🇨🇳")
)

fun getCurrentLanguageCode(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    return if (locales.isEmpty) {
        java.util.Locale.getDefault().language
    } else {
        // e.g. "pt_BR" -> "pt-BR" or just "en"
        val tag = locales[0]?.toLanguageTag() ?: "en"
        // Match "pt-BR" specifically; otherwise return just language
        supportedLanguages.firstOrNull { it.code == tag }?.code
            ?: locales[0]?.language
            ?: "en"
    }
}

fun setAppLanguage(languageCode: String) {
    val localeList = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(localeList)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerDialog(onDismiss: () -> Unit) {
    val currentCode = remember { getCurrentLanguageCode() }
    var selectedCode by remember { mutableStateOf(currentCode) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Select Language",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(supportedLanguages) { lang ->
                        val isSelected = lang.code == selectedCode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color.Transparent,
                            onClick = { selectedCode = lang.code }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lang.flag,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Apply Button
                Button(
                    onClick = {
                        setAppLanguage(selectedCode)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedCode != currentCode
                ) {
                    Text(
                        text = "Apply",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
