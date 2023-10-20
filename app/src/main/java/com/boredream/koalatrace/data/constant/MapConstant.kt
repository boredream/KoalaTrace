package com.boredream.koalatrace.data.constant

object MapConstant {

    /**
     * 绘制区域时，最小可忽略内环面积（大概70米*70米）
     */
    const val IGNORE_INTER_RING_AREA = 5E-7

    /**
     * 区域分割正方形后，和形状区划交集 / 正方形面积的忽略比例阈值
     */
    const val AREA_SPLIT_IGNORE_AREA_RATIO = 0.15

    /**
     * 区域分割正方形边长（单位：m）
     */
    const val AREA_SPLIT_SQUARE_LENGTH = 1000

}