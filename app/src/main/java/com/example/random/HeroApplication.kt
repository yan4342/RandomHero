package com.example.random

import android.app.Application
import com.example.random.data.HeroDataSource
import com.example.random.data.HeroRepository
import com.example.random.data.ImagePreloader

/**
 * 自定义 Application 类
 * 负责在应用启动时初始化数据源
 */
class HeroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化数据源
        HeroDataSource.init(this)
        // 加载英雄数据到仓库
        HeroRepository.init(this)
        // 初始化图片预加载器
        ImagePreloader.init(this)
    }
}
