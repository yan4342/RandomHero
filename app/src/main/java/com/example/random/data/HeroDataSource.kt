package com.example.random.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.random.model.Hero
import com.example.random.model.HeroCombo
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * 英雄数据源管理类
 * 负责数据的加载、保存、增删改、导入导出
 * 
 * 数据优先级：SharedPreferences 用户数据 → assets/heroes.json → 硬编码默认值
 * 默认数据永不修改，用户修改存储在 SharedPreferences 独立 key 中
 */
object HeroDataSource {

    private const val PREFS_NAME = "hero_data"
    private const val KEY_HEROES = "user_heroes"
    private const val KEY_DELETED = "deleted_hero_ids"

    private lateinit var prefs: SharedPreferences

    /**
     * 初始化，在 Application.onCreate() 中调用
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── 英雄数据加载 ──────────────────────────────────────────────────────

    /**
     * 加载英雄列表（合并用户修改和默认数据）
     */
    fun loadHeroes(context: Context): List<Hero> {
        val defaults = loadDefaultHeroes(context)
        val userHeroesJson = prefs.getString(KEY_HEROES, null)
        val deletedIds = loadDeletedIds()

        // 用户自定义的英雄（修改过的或新增的）
        val userHeroes = if (userHeroesJson != null) {
            parseHeroesJson(userHeroesJson)
        } else {
            emptyList()
        }

        // 用户新增的英雄（不在默认列表中的 ename）
        val defaultEnames = defaults.map { it.ename }.toSet()
        val addedHeroes = userHeroes.filter { it.ename !in defaultEnames }

        // 合并：默认英雄（排除已删除的）中，用用户修改覆盖分路信息，加上用户新增的
        val result = mutableListOf<Hero>()
        for (default in defaults) {
            if (default.ename in deletedIds) continue
            val userModified = userHeroes.find { it.ename == default.ename }
            if (userModified != null) {
                result.add(userModified)
            } else {
                result.add(default)
            }
        }
        result.addAll(addedHeroes)

        return result
    }

    /**
     * 从 assets/heroes.json 加载默认英雄数据
     */
    private fun loadDefaultHeroes(context: Context): List<Hero> {
        return try {
            val json = context.assets.open("heroes.json").bufferedReader().use { it.readText() }
            parseHeroesJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 从 JSON 字符串解析英雄列表
     */
    private fun parseHeroesJson(json: String): List<Hero> {
        val result = mutableListOf<Hero>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(Hero(
                    ename = obj.getInt("ename"),
                    cname = obj.getString("cname"),
                    idName = obj.getString("idName"),
                    title = obj.getString("title"),
                    heroType = obj.getInt("heroType"),
                    heroType2 = if (obj.has("heroType2") && !obj.isNull("heroType2")) obj.getInt("heroType2") else null,
                    heroType3 = if (obj.has("heroType3") && !obj.isNull("heroType3")) obj.getInt("heroType3") else null,
                    skinName = obj.optString("skinName", ""),
                    mossId = obj.optInt("mossId", 0),
                    payType = if (obj.has("payType") && !obj.isNull("payType")) obj.getInt("payType") else null
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // ── 英雄数据保存 ──────────────────────────────────────────────────────

    /**
     * 保存完整的英雄列表到 SharedPreferences
     */
    fun saveHeroes(heroes: List<Hero>) {
        val json = heroesToJson(heroes)
        prefs.edit().putString(KEY_HEROES, json).apply()
    }

    /**
     * 保存单个英雄（新增或修改）
     */
    fun saveHero(hero: Hero) {
        val allHeroes = loadHeroesFromPrefs().toMutableList()
        val index = allHeroes.indexOfFirst { it.ename == hero.ename }
        if (index >= 0) {
            allHeroes[index] = hero
        } else {
            allHeroes.add(hero)
        }
        saveHeroes(allHeroes)
    }

    /**
     * 删除英雄（标记为已删除，不影响默认数据）
     */
    fun deleteHero(ename: Int) {
        val deletedIds = loadDeletedIds().toMutableSet()
        deletedIds.add(ename)
        saveDeletedIds(deletedIds)

        // 同时从用户数据中移除
        val userHeroes = loadHeroesFromPrefs().toMutableList()
        userHeroes.removeAll { it.ename == ename }
        saveHeroes(userHeroes)
    }

    /**
     * 重置为默认数据（清除所有用户修改）
     */
    fun resetToDefault() {
        prefs.edit()
            .remove(KEY_HEROES)
            .remove(KEY_DELETED)
            .apply()
    }

    /**
     * 检查是否有用户自定义数据
     */
    fun hasUserData(): Boolean {
        return prefs.contains(KEY_HEROES) || prefs.contains(KEY_DELETED)
    }

    // ── 已删除 ID 管理 ────────────────────────────────────────────────────

    private fun loadDeletedIds(): Set<Int> {
        val json = prefs.getString(KEY_DELETED, null) ?: return emptySet()
        val result = mutableSetOf<Int>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                result.add(jsonArray.getInt(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun saveDeletedIds(ids: Set<Int>) {
        val jsonArray = JSONArray()
        ids.forEach { jsonArray.put(it) }
        prefs.edit().putString(KEY_DELETED, jsonArray.toString()).apply()
    }

    // ── SharedPreferences 辅助 ─────────────────────────────────────────────

    private fun loadHeroesFromPrefs(): List<Hero> {
        val json = prefs.getString(KEY_HEROES, null) ?: return emptyList()
        return parseHeroesJson(json)
    }

    private fun heroesToJson(heroes: List<Hero>): String {
        val jsonArray = JSONArray()
        for (hero in heroes) {
            val obj = JSONObject().apply {
                put("ename", hero.ename)
                put("cname", hero.cname)
                put("idName", hero.idName)
                put("title", hero.title)
                put("heroType", hero.heroType)
                hero.heroType2?.let { put("heroType2", it) }
                hero.heroType3?.let { put("heroType3", it) }
                put("skinName", hero.skinName)
                put("mossId", hero.mossId)
                hero.payType?.let { put("payType", it) }
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    // ── 组合数据加载（只读，不支持编辑） ───────────────────────────────────

    /**
     * 加载英雄组合（始终从 assets 加载）
     */
    fun loadCombos(context: Context): List<HeroCombo> {
        return try {
            val json = context.assets.open("combos.json").bufferedReader().use { it.readText() }
            parseCombosJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseCombosJson(json: String): List<HeroCombo> {
        val result = mutableListOf<HeroCombo>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val heroIdsArray = obj.getJSONArray("heroIds")
                val heroIds = mutableListOf<Int>()
                for (j in 0 until heroIdsArray.length()) {
                    heroIds.add(heroIdsArray.getInt(j))
                }
                result.add(HeroCombo(
                    name = obj.getString("name"),
                    heroIds = heroIds,
                    minRequired = obj.getInt("minRequired"),
                    borderColor = obj.getString("borderColor")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // ── 导入 / 导出 ───────────────────────────────────────────────────────

    /**
     * 导出当前配置到 URI
     */
    fun exportConfig(context: Context, uri: Uri): Boolean {
        return try {
            val heroes = loadHeroes(context)
            val deletedIds = loadDeletedIds()

            val config = JSONObject().apply {
                put("heroes", JSONArray(heroesToJson(heroes)))
                put("deletedIds", JSONArray().apply {
                    deletedIds.forEach { put(it) }
                })
                put("version", 1)
                put("exportTime", System.currentTimeMillis())
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(config.toString(2))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 从 URI 导入配置
     */
    fun importConfig(context: Context, uri: Uri): Boolean {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).readText()
            } ?: return false

            val config = JSONObject(json)
            val version = config.optInt("version", 1)

            if (version == 1) {
                // 保存英雄数据
                val heroesArray = config.getJSONArray("heroes")
                val heroes = parseHeroesJson(heroesArray.toString())
                saveHeroes(heroes)

                // 保存已删除 ID
                val deletedArray = config.optJSONArray("deletedIds")
                if (deletedArray != null) {
                    val deletedIds = mutableSetOf<Int>()
                    for (i in 0 until deletedArray.length()) {
                        deletedIds.add(deletedArray.getInt(i))
                    }
                    saveDeletedIds(deletedIds)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
