package com.example.random.model

data class ShareResultData(
    val mode: String = "role",
    val teamA: List<TeamSlot> = emptyList(),
    val teamB: List<TeamSlot> = emptyList(),
    val banList: List<Hero?> = emptyList(),
    val activeCombos: List<HeroCombo> = emptyList()
)
