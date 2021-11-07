package com.kelin.skuselector.sku

import com.kelin.skuselector.model.TagModel

/**
 * **描述:** 描述属性的基本特征。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2021/11/5 3:56 PM
 *
 * **版本:** v 1.0.0
 */
interface Spec : TagModel {
    /**
     * 当前属性所属分组的分组ID。
     */
    val groupId: String

    /**
     * 当前属性的ID。
     */
    val specId: String
}