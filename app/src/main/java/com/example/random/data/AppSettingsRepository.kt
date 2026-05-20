package com.example.random.data

import android.content.Context
import android.content.SharedPreferences
import com.example.random.model.ThemeColorSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppSettingsRepository {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_ACCENT_COLOR = "accentColor"
    private const val KEY_TEAM_A_COLOR = "teamAColor"
    private const val KEY_TEAM_B_COLOR = "teamBColor"
    private const val KEY_BACKGROUND_COLOR = "backgroundColor"
    private const val KEY_CARD_COLOR = "cardColor"

    private lateinit var prefs: SharedPreferences

    private val _themeColors = MutableStateFlow(ThemeColorSettings.Default)
    val themeColors: StateFlow<ThemeColorSettings> = _themeColors.asStateFlow()

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _themeColors.value = loadThemeColors()
    }

    fun updateThemeColors(
        accentColor: Int = _themeColors.value.accentColor,
        teamAColor: Int = _themeColors.value.teamAColor,
        teamBColor: Int = _themeColors.value.teamBColor,
        backgroundColor: Int? = _themeColors.value.backgroundColor,
        cardColor: Int? = _themeColors.value.cardColor
    ) {
        val next = ThemeColorSettings(
            accentColor = accentColor,
            teamAColor = teamAColor,
            teamBColor = teamBColor,
            backgroundColor = backgroundColor,
            cardColor = cardColor
        )

        prefs.edit().apply {
            putInt(KEY_ACCENT_COLOR, next.accentColor)
            putInt(KEY_TEAM_A_COLOR, next.teamAColor)
            putInt(KEY_TEAM_B_COLOR, next.teamBColor)
            putOptionalColor(KEY_BACKGROUND_COLOR, next.backgroundColor)
            putOptionalColor(KEY_CARD_COLOR, next.cardColor)
            apply()
        }

        _themeColors.value = next
    }

    fun resetThemeColors() {
        prefs.edit()
            .remove(KEY_ACCENT_COLOR)
            .remove(KEY_TEAM_A_COLOR)
            .remove(KEY_TEAM_B_COLOR)
            .remove(KEY_BACKGROUND_COLOR)
            .remove(KEY_CARD_COLOR)
            .apply()

        _themeColors.value = ThemeColorSettings.Default
    }

    private fun loadThemeColors(): ThemeColorSettings {
        return ThemeColorSettings(
            accentColor = prefs.getInt(
                KEY_ACCENT_COLOR,
                ThemeColorSettings.DEFAULT_ACCENT_COLOR
            ),
            teamAColor = prefs.getInt(
                KEY_TEAM_A_COLOR,
                ThemeColorSettings.DEFAULT_TEAM_A_COLOR
            ),
            teamBColor = prefs.getInt(
                KEY_TEAM_B_COLOR,
                ThemeColorSettings.DEFAULT_TEAM_B_COLOR
            ),
            backgroundColor = prefs.getOptionalColor(KEY_BACKGROUND_COLOR),
            cardColor = prefs.getOptionalColor(KEY_CARD_COLOR)
        )
    }

    private fun SharedPreferences.Editor.putOptionalColor(key: String, color: Int?) {
        if (color == null) {
            remove(key)
        } else {
            putInt(key, color)
        }
    }

    private fun SharedPreferences.getOptionalColor(key: String): Int? {
        return if (contains(key)) getInt(key, 0) else null
    }
}
