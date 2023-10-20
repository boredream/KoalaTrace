package com.boredream.koalatrace.data

enum class ExploreLevel(
    val percent: Float,
    val description: String,
) {
    NOT_VISIT(0.00f, "陌生区域"),
    VISITED(0.05f, "访问过"),
    EXPLORED(0.30f, "初步探索"),
    FAMILIAR(0.70f, "十分熟悉"),
    CONQUERED(0.95f, "完全探索"),
}