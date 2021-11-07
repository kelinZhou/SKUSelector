package com.kelin.skuselector.sku

/**
 * **描述:** 描述SKU特点。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2021/11/5 3:54 PM
 *
 * **版本:** v 1.0.0
 */
interface SKU {
    fun hasSpecs(specs: Collection<Spec>): Boolean
}