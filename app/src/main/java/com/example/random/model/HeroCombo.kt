package com.example.random.model

/**
 * 英雄组合定义
 * @param name 组合名称，如"长城守卫军"
 * @param heroIds 组合中包含的英雄ID列表
 * @param minRequired 触发所需的最小英雄数量，默认3（组合总人数大于等于3时，只包含3个英雄就可以触发）
 * @param borderColor 特殊边框颜色（可选，十六进制格式）
 */
data class HeroCombo(
    val name: String,
    val heroIds: List<Int>,
    val minRequired: Int = 3,
    val borderColor: String = "#FFD700" // 默认金色边框
)