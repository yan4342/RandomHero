package com.example.random.model

data class ThemeColorSettings(
    val accentColor: Int = DEFAULT_ACCENT_COLOR,
    val teamAColor: Int = DEFAULT_TEAM_A_COLOR,
    val teamBColor: Int = DEFAULT_TEAM_B_COLOR,
    val backgroundColor: Int? = null,
    val cardColor: Int? = null
) {
    companion object {
        val DEFAULT_ACCENT_COLOR: Int = 0xFFBCA676.toInt()
        val DEFAULT_TEAM_A_COLOR: Int = 0xFF3D8BFD.toInt()
        val DEFAULT_TEAM_B_COLOR: Int = 0xFFE33E33.toInt()

        val Default = ThemeColorSettings()
    }
}

data class MaterialColorPreset(
    val name: String,
    val color: Int
)

val MaterialColorPresets = listOf(
    MaterialColorPreset("Blue 600", 0xFF1E88E5.toInt()),
    MaterialColorPreset("Red 600", 0xFFE53935.toInt()),
    MaterialColorPreset("Teal 600", 0xFF00897B.toInt()),
    MaterialColorPreset("Purple 500", 0xFF9C27B0.toInt()),
    MaterialColorPreset("Green 600", 0xFF43A047.toInt()),
    MaterialColorPreset("Amber 700", 0xFFFFA000.toInt()),
    MaterialColorPreset("Deep Orange 600", 0xFFF4511E.toInt()),
    MaterialColorPreset("Blue Grey 600", 0xFF546E7A.toInt())
)
