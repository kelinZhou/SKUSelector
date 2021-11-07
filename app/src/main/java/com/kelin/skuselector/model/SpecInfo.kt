package com.kelin.skuselector.model

import com.kelin.skuselector.sku.Spec

/**
 * **描述:** 属性。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2021/11/5 2:46 PM
 *
 * **版本:** v 1.0.0
 */
data class SpecInfo(
    /**
     * 当前属性分组的ID。
     */
    override val groupId: String,
    /**
     * 当前属性分组的名称。
     */
    val groupName: String,
    /**
     * 当前属性的ID。
     */
    override val specId: String,
    /**
     * 当前属性的名称。
     */
    val specName: String
) : Spec {
    /**
     * 默认是没有选中的。
     */
    override var isSelected: Boolean = false

    /**
     * 默认是可用的。
     */
    override var isEnable: Boolean = true

    /**
     * 将属性名称显示给用户。
     */
    override val showText: String
        get() = specName
}