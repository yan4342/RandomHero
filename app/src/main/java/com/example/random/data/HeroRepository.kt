package com.example.random.data

import android.content.Context
import com.example.random.model.Hero
import com.example.random.model.HeroCombo
//1 to "对抗路", 2 to "中路", 3 to "游走",4 to "打野",5 to "发育路",
//Download the official hero list from `https://pvp.qq.com/web201605/js/herolist.json`
object HeroRepository {

    private var _heroList: List<Hero> = emptyList()
    private var _heroCombos: List<HeroCombo> = emptyList()

    /** 当前英雄列表（由 HeroDataSource 提供，可能包含用户修改） */
    val heroList: List<Hero> get() = _heroList

    /** 英雄组合（始终从 assets 加载） */
    val heroCombos: List<HeroCombo> get() = _heroCombos

    /**
     * 初始化数据，在 Application.onCreate() 中调用
     */
    fun init(context: Context) {
        _heroList = HeroDataSource.loadHeroes(context)
        _heroCombos = HeroDataSource.loadCombos(context)
    }

    /**
     * 重新加载数据（设置页保存后调用）
     */
    fun reload(context: Context) {
        _heroList = HeroDataSource.loadHeroes(context)
    }

    /**
     * 获取英雄头像 URL
     */
    fun getHeroAvatarUrl(ename: Int): String {
        return "https://game.gtimg.cn/images/yxzj/img201606/heroimg/$ename/$ename.jpg"
    }
}
