package com.example.random.data

import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 图片预加载器
 * 使用全局 ImageLoader 实例预加载图片，与 HeroAvatar 共享缓存
 */
object ImagePreloader {
    
    private var context: Context? = null
    
    /**
     * 初始化，保存 Context 引用
     * 实际的 ImageLoader 由 HeroApplication 创建并注册为全局单例
     */
    fun init(context: Context) {
        this.context = context.applicationContext
    }
    
    /**
     * 获取全局 ImageLoader 实例
     */
    private fun getImageLoader(): ImageLoader {
        return Coil.imageLoader(context!!)
    }
    
    /**
     * 预加载单个英雄头像
     */
    fun preloadHeroAvatar(context: Context, ename: Int) {
        val imageUrl = HeroRepository.getHeroAvatarUrl(ename)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .memoryCacheKey(imageUrl) // 固定缓存 key，与 HeroAvatar 共享缓存
                    .diskCacheKey(imageUrl)
                    .build()
                
                getImageLoader().enqueue(request)
            } catch (e: Exception) {
                // 静默失败，不影响主流程
            }
        }
    }
    
    /**
     * 批量预加载英雄头像
     */
    fun preloadHeroAvatars(context: Context, heroes: List<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            heroes.forEach { ename ->
                try {
                    val imageUrl = HeroRepository.getHeroAvatarUrl(ename)
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .memoryCacheKey(imageUrl) // 固定缓存 key，与 HeroAvatar 共享缓存
                        .diskCacheKey(imageUrl)
                        .build()
                    
                    getImageLoader().enqueue(request)
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
     * 注意：不再关闭 ImageLoader，因为它是全局共享的
     */
    fun clear() {
        context = null
    }
}