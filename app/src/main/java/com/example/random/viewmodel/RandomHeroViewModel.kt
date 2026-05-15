package com.example.random.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.random.data.HeroRepository
import com.example.random.model.Hero
import com.example.random.model.HeroCombo
import com.example.random.model.Position
import com.example.random.model.ShareResultData
import com.example.random.model.TeamSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GameMode { ROLE_BASED, RANDOM }

class RandomHeroViewModel : ViewModel() {

    private val _mode = MutableStateFlow(GameMode.ROLE_BASED)
    val mode: StateFlow<GameMode> = _mode.asStateFlow()
    
    private val _teamA = MutableStateFlow<List<TeamSlot>>(emptyList())
    val teamA: StateFlow<List<TeamSlot>> = _teamA.asStateFlow()
    
    private val _teamB = MutableStateFlow<List<TeamSlot>>(emptyList())
    val teamB: StateFlow<List<TeamSlot>> = _teamB.asStateFlow()
    
    private val _banList = MutableStateFlow<List<Hero?>>(List(8) { null })
    val banList: StateFlow<List<Hero?>> = _banList.asStateFlow()
    
    private val _showSelector = MutableStateFlow(false)
    val showSelector: StateFlow<Boolean> = _showSelector.asStateFlow()
    
    private val _currentBanIndex = MutableStateFlow(-1)
    val currentBanIndex: StateFlow<Int> = _currentBanIndex.asStateFlow()
    
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    private val _filteredHeroes = MutableStateFlow<List<Hero>>(emptyList())
    val filteredHeroes: StateFlow<List<Hero>> = _filteredHeroes.asStateFlow()
    
    private val _activeCombos = MutableStateFlow<List<HeroCombo>>(emptyList())
    val activeCombos: StateFlow<List<HeroCombo>> = _activeCombos.asStateFlow()
    
    private val _showShareScreen = MutableStateFlow(false)
    val showShareScreen: StateFlow<Boolean> = _showShareScreen.asStateFlow()

    private val _expandBanBar = MutableStateFlow(false)
    val expandBanBar: StateFlow<Boolean> = _expandBanBar.asStateFlow()

    private val _autoShare = MutableStateFlow(false)
    val autoShare: StateFlow<Boolean> = _autoShare.asStateFlow()
    
    init {
        randomize()
    }
    
    fun setMode(newMode: GameMode) {
        _mode.value = newMode
        randomize()
    }
    
    private fun getHeroByPriority(position: Position, availableHeroes: List<Hero>): Hero? {
        if (availableHeroes.isEmpty()) return null
        
        // 权重配置
        val WEIGHT_PRIMARY = 0.6   // herotype匹配的权重
        val WEIGHT_SECONDARY = 0.4 // herotype2匹配的权重
        val WEIGHT_TERTIARY = 0.3  // herotype3匹配的权重
        
        // 计算每个英雄的权重
        val weightedHeroes = mutableListOf<Pair<Hero, Double>>()
        
        for (hero in availableHeroes) {
            var weight = 0.0
            
            // 检查herotype匹配（最高优先级）
            if (position.roles.contains(hero.heroType)) {
                weight = WEIGHT_PRIMARY
            }
            // 检查herotype2匹配（如果不匹配herotype且herotype2不为null）
            else if (hero.heroType2 != null && position.roles.contains(hero.heroType2)) {
                weight = WEIGHT_SECONDARY
            }
            // 检查herotype3匹配（如果不匹配herotype和herotype2且herotype3不为null）
            else if (hero.heroType3 != null && position.roles.contains(hero.heroType3)) {
                weight = WEIGHT_TERTIARY
            }
            
            if (weight > 0) {
                weightedHeroes.add(hero to weight)
            }
        }
        
        if (weightedHeroes.isEmpty()) return null
        
        // 计算总权重
        val totalWeight = weightedHeroes.sumOf { it.second }
        
        // 如果总权重为0，返回null
        if (totalWeight <= 0.0) return null
        
        // 加权随机选择
        var randomValue = kotlin.random.Random.nextDouble() * totalWeight
        for ((hero, weight) in weightedHeroes) {
            randomValue -= weight
            if (randomValue <= 0) {
                return hero
            }
        }
        
        // 作为备用，返回最后一个英雄
        return weightedHeroes.last().first
    }

    private fun buildTeamSlot(positionName: String, hero: Hero?): TeamSlot {
        return TeamSlot(
            positionName = positionName,
            name = hero?.cname ?: "无英雄",
            avatarUrl = hero?.let { HeroRepository.getHeroAvatarUrl(it.ename) } ?: "",
            ename = hero?.ename ?: 0
        )
    }

    private fun collectUsedEnames(): MutableSet<Int> {
        val usedEnames = mutableSetOf<Int>()
        _banList.value.forEach { hero -> hero?.ename?.let { usedEnames.add(it) } }
        _teamA.value.forEach { slot -> if (slot.ename != 0) usedEnames.add(slot.ename) }
        _teamB.value.forEach { slot -> if (slot.ename != 0) usedEnames.add(slot.ename) }
        return usedEnames
    }

    fun randomize() {
        viewModelScope.launch {
            val allHeroes = HeroRepository.heroList
            val usedEnames = collectUsedEnames()

            val teamA = mutableListOf<TeamSlot>()
            val teamB = mutableListOf<TeamSlot>()

            val getRandomHero = { filterFn: ((Hero) -> Boolean)? ->
                val available = allHeroes.filter { hero ->
                    !usedEnames.contains(hero.ename) && (filterFn?.invoke(hero) ?: true)
                }
                if (available.isEmpty()) null
                else available.random().also { usedEnames.add(it.ename) }
            }

            if (_mode.value == GameMode.ROLE_BASED) {
                Hero.POSITIONS.forEach { position ->
                    // Team A - 使用优先级选择
                    val availableForTeamA = allHeroes.filter { hero ->
                        !usedEnames.contains(hero.ename)
                    }
                    val heroA = getHeroByPriority(position, availableForTeamA)
                    if (heroA != null) {
                        usedEnames.add(heroA.ename)
                    }
                    teamA.add(buildTeamSlot(position.name, heroA))

                    // Team B - 使用优先级选择
                    val availableForTeamB = allHeroes.filter { hero ->
                        !usedEnames.contains(hero.ename)
                    }
                    val heroB = getHeroByPriority(position, availableForTeamB)
                    if (heroB != null) {
                        usedEnames.add(heroB.ename)
                    }
                    teamB.add(buildTeamSlot(position.name, heroB))
                }
            } else {
                // Random mode
                repeat(5) { index ->
                    val heroA = getRandomHero(null)
                    teamA.add(buildTeamSlot("位置 ${index + 1}", heroA))

                    val heroB = getRandomHero(null)
                    teamB.add(buildTeamSlot("位置 ${index + 1}", heroB))
                }
            }

            _teamA.value = teamA
            _teamB.value = teamB
            detectActiveCombos()
        }
    }
    
    private fun detectActiveCombos() {
        val allHeroIds = mutableSetOf<Int>()
        
        // 收集团队A中的英雄ID
        _teamA.value.forEach { slot ->
            if (slot.ename != 0) allHeroIds.add(slot.ename)
        }
        
        // 收集团队B中的英雄ID
        _teamB.value.forEach { slot ->
            if (slot.ename != 0) allHeroIds.add(slot.ename)
        }
        
        // 收集ban位中的英雄ID
        _banList.value.forEach { hero ->
            hero?.ename?.let { allHeroIds.add(it) }
        }
        
        val active = mutableListOf<HeroCombo>()
        
        // 检查所有组合
        for (combo in HeroRepository.heroCombos) {
            val matchedIds = combo.heroIds.intersect(allHeroIds)
            if (matchedIds.size >= combo.minRequired) {
                active.add(combo)
            }
        }
        
        _activeCombos.value = active
    }
    
    fun selectBanHero(index: Int) {
        _currentBanIndex.value = index
        _showSelector.value = true
        _filteredHeroes.value = HeroRepository.heroList
        _searchText.value = ""
    }
    
    fun closeSelector() {
        _showSelector.value = false
        _currentBanIndex.value = -1
    }
    
    fun onSearchInput(text: String) {
        _searchText.value = text
        val filtered = HeroRepository.heroList.filter { hero ->
            hero.cname.contains(text) || hero.title.contains(text)
        }
        _filteredHeroes.value = filtered
    }
    
    fun confirmBan(hero: Hero) {
        val index = _currentBanIndex.value
        if (index == -1) return
        
        // Check if already banned
        if (_banList.value.any { it?.ename == hero.ename }) {
            // Show toast (would need context)
            return
        }
        
        val newBanList = _banList.value.toMutableList()
        newBanList[index] = hero
        _banList.value = newBanList
        _showSelector.value = false
        
        randomize()
    }
    
    fun removeBan(index: Int) {
        val newBanList = _banList.value.toMutableList()
        newBanList[index] = null
        _banList.value = newBanList
        randomize()
    }
    
    fun reRollOne(team: String, index: Int) {
        viewModelScope.launch {
            val allHeroes = HeroRepository.heroList
            val usedEnames = collectUsedEnames()

            val isTeamA = team == "A"
            val currentTeam = if (isTeamA) _teamA.value else _teamB.value
            val targetSlot = currentTeam[index]

            // Remove current slot's hero from used set so it can be re-rolled to someone else
            // but we want a NEW hero, so we keep it in usedEnames to avoid rolling it again.
            // Also, we want to check if the hero is banned or already in the team.
            var newHero: Hero? = null
            if (_mode.value == GameMode.ROLE_BASED) {
                val posName = targetSlot.positionName
                val posDef = Hero.POSITIONS.find { it.name == posName }
                if (posDef != null) {
                    val available = allHeroes.filter { hero -> !usedEnames.contains(hero.ename) }
                    newHero = getHeroByPriority(posDef, available)
                }
            } else {
                val available = allHeroes.filter { hero -> !usedEnames.contains(hero.ename) }
                if (available.isNotEmpty()) {
                    newHero = available.random()
                }
            }

            if (newHero != null) {
                val newSlot = buildTeamSlot(targetSlot.positionName, newHero)

                if (isTeamA) {
                    val newList = _teamA.value.toMutableList()
                    newList[index] = newSlot
                    _teamA.value = newList
                } else {
                    val newList = _teamB.value.toMutableList()
                    newList[index] = newSlot
                    _teamB.value = newList
                }
                detectActiveCombos()
            }
        }
    }
    
    fun openShareScreen(expandBan: Boolean = false, autoShare: Boolean = false) {
        _expandBanBar.value = expandBan
        _autoShare.value = autoShare
        _showShareScreen.value = true
    }
    
    fun closeShareScreen() {
        _showShareScreen.value = false
        _autoShare.value = false
    }
    
    fun getShareData(): ShareResultData {
        return ShareResultData(
            mode = if (_mode.value == GameMode.ROLE_BASED) "role" else "random",
            teamA = _teamA.value,
            teamB = _teamB.value,
            banList = _banList.value,
            activeCombos = _activeCombos.value
        )
    }
}