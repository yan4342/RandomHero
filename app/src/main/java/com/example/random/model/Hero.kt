package com.example.random.model

data class Hero(
    val ename: Int,
    val cname: String,
    val idName: String,
    val title: String,
    val heroType: Int,
    val heroType2: Int? = null,
    val heroType3: Int? = null,
    val skinName: String,
    val mossId: Int,
    val payType: Int? = null
) {
    companion object {
        val ROLES = mapOf(
            1 to "对抗路",
            2 to "中路", 
            3 to "游走",
            4 to "打野",
            5 to "发育路",
        )
        
        val POSITIONS = listOf(
            Position("对抗路", listOf(1)),
            Position("打野", listOf(4)),
            Position("中路", listOf(2)),
            Position("发育路", listOf(5)),
            Position("游走", listOf(3))
        )
    }
}

data class Position(
    val name: String,
    val roles: List<Int>
)

data class TeamSlot(
    val positionName: String,
    val name: String = "待抽取",
    val avatarUrl: String = "",
    val ename: Int = 0
)