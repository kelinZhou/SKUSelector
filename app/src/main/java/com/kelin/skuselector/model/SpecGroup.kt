package com.kelin.skuselector.model

/**
 * **描述:** 定义属性组。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2021/11/5 2:55 PM
 *
 * **版本:** v 1.0.0
 */
data class SpecGroup(
    val name: String,
    val specs: List<SpecInfo>
)