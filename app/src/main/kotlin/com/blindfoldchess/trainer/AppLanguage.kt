package com.blindfoldchess.trainer

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.blindfoldchess.trainer.core.chess.ChessMoveAnnouncer
import java.util.Locale

enum class AppLanguage(val tag: String) {
    French("fr-FR"),
    English("en-US"),
    ;

    val locale: Locale get() = Locale.forLanguageTag(tag)

    val announcer: ChessMoveAnnouncer.Language
        get() = when (this) {
            French -> ChessMoveAnnouncer.Language.French
            English -> ChessMoveAnnouncer.Language.English
        }

    companion object {
        private const val PREFS = "app_language"
        private const val KEY = "language_tag"
        private const val LEGACY_PREFS = "voice_input"
        private const val LEGACY_KEY = "language_tag"

        fun fromTag(tag: String?): AppLanguage {
            if (tag.isNullOrBlank()) return defaultForDevice()
            val language = tag.substringBefore('-').lowercase(Locale.ROOT)
            return when (language) {
                "fr" -> French
                "en" -> English
                else -> defaultForDevice()
            }
        }

        fun defaultForDevice(): AppLanguage =
            if (Locale.getDefault().language == "fr") French else English

        fun load(context: Context): AppLanguage {
            val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, null)
            if (stored != null) return fromTag(stored)
            val legacy = context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
                .getString(LEGACY_KEY, null)
            val loaded = fromTag(legacy)
            save(context, loaded)
            return loaded
        }

        fun save(context: Context, language: AppLanguage) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, language.tag)
                .apply()
            context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(LEGACY_KEY, language.tag)
                .apply()
        }
    }
}

class AppLanguageState(
    initial: AppLanguage,
    private val persist: (AppLanguage) -> Unit,
) {
    var language by mutableStateOf(initial)
        private set

    fun update(next: AppLanguage) {
        if (next == language) return
        language = next
        persist(next)
    }
}

val LocalAppLanguageState = staticCompositionLocalOf<AppLanguageState?> { null }

fun Context.withAppLanguage(language: AppLanguage): Context {
    val config = Configuration(resources.configuration)
    config.setLocales(android.os.LocaleList(language.locale))
    val localized = createConfigurationContext(config)
    // Wrap this context (the Activity) so Compose can still walk to
    // ActivityResultRegistryOwner / SpeechRecognizer. Returning the
    // configuration context directly is not a ContextWrapper of the Activity
    // and crashes screens that request the microphone.
    return object : ContextWrapper(this) {
        override fun getResources(): Resources = localized.resources
        override fun getAssets(): AssetManager = localized.assets
    }
}

@Composable
fun rememberAppLanguageState(): AppLanguageState {
    val context = LocalContext.current.applicationContext
    return remember {
        AppLanguageState(AppLanguage.load(context)) { AppLanguage.save(context, it) }
    }
}

@Composable
fun ProvideAppLanguage(
    state: AppLanguageState,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val localized = remember(context, state.language) {
        context.withAppLanguage(state.language)
    }
    SideEffect {
        Locale.setDefault(state.language.locale)
    }
    CompositionLocalProvider(
        LocalAppLanguageState provides state,
        LocalContext provides localized,
        LocalConfiguration provides localized.resources.configuration,
        content = content,
    )
}

@Composable
fun rememberAppLanguage(): Pair<AppLanguage, (AppLanguage) -> Unit> {
    val provided = LocalAppLanguageState.current
    val context = LocalContext.current.applicationContext
    val fallback = remember {
        AppLanguageState(AppLanguage.load(context)) { AppLanguage.save(context, it) }
    }
    val state = provided ?: fallback
    return state.language to state::update
}
