package com.example.random

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.random.data.AppSettingsRepository
import com.example.random.data.HeroDataSource
import com.example.random.data.HeroRepository
import com.example.random.data.ImagePreloader

/**
 * 自定义 Application 类
 * 负责在应用启动时初始化数据源和全局图片加载器
 */
class HeroApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // 初始化应用设置
        AppSettingsRepository.init(this)
        // 初始化数据源
        HeroDataSource.init(this)
        // 加载英雄数据到仓库
        HeroRepository.init(this)
        // 初始化图片预加载器（现在使用全局 ImageLoader）
        ImagePreloader.init(this)
    }

    /**
     * 创建全局 ImageLoader 实例
     * Coil 会自动使用此实例作为默认的图片加载器
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.2) // 使用 20% 的可用内存，头像总量约5MB
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("hero_avatar_cache"))
                    .maxSizeBytes(10 * 1024 * 1024) // 10MB 磁盘缓存，足够存放全部头像
                    .build()
            }
            .build()
    }
}
