package com.example.random.data

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 图片预加载器
 * 用于在后台预加载图片，减少滚动时的卡顿
 */
object ImagePreloader {
    
    private var imageLoader: ImageLoader? = null
    
    /**
     * 初始化图片加载器
     */
    fun init(context: Context) {
        if (imageLoader == null) {
            imageLoader = ImageLoader.Builder(context)
                .crossfade(true)
                .build()
        }
    }
    
    /**
     * 预加载单个英雄头像
     */
    fun preloadHeroAvatar(context: Context, ename: Int) {
        val loader = imageLoader ?: ImageLoader.Builder(context)
            .crossfade(true)
            .build().also { imageLoader = it }
        
        val imageUrl = HeroRepository.getHeroAvatarUrl(ename)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(50, 50) // 预加载缩略图尺寸
                    .build()
                
                loader.enqueue(request)
            } catch (e: Exception) {
                // 静默失败，不影响主流程
            }
        }
    }
    
    /**
     * 批量预加载英雄头像
     */
    fun preloadHeroAvatars(context: Context, heroes: List<Int>) {
        val loader = imageLoader ?: ImageLoader.Builder(context)
            .crossfade(true)
            .build().also { imageLoader = it }
        
        CoroutineScope(Dispatchers.IO).launch {
            heroes.forEach { ename ->
                try {
                    val imageUrl = HeroRepository.getHeroAvatarUrl(ename)
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .size(50, 50) // 预加载缩略图尺寸
                        .build()
                    
                    loader.enqueue(request)
                } catch (e: Exception) {
                    // 静默失败，继续预加载其他图片
                }
            }
        }
    }
    
    /**
     * 预加载所有英雄头像
     */
    fun preloadAllHeroAvatars(context: Context) {
        val heroes = HeroRepository.heroList.map { it.ename }
        preloadHeroAvatars(context, heroes)
    }
    
    /**
     * 清理资源
     */
    fun clear() {
        imageLoader?.shutdown()
        imageLoader = null
    }
}