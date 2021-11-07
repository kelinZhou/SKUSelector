package com.kelin.skuselector.model

/**
 * **描述:** 标签模型。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2021-08-17 11:30:21
 *
 * **版本:** v 1.0.0
 */
interface TagModel {
    /**
     * 是否已选中。
     */
    var isSelected: Boolean

    /**
     * 是否可用(非禁用)。
     */
    var isEnable: Boolean

    /**
     * 显示文字。
     */
    val showText: String
}