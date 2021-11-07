package com.kelin.skuselector.model

import com.kelin.skuselector.sku.SKU
import com.kelin.skuselector.sku.Spec

/**
 * **描述:** 商品信息。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2021/11/5 2:49 PM
 *
 * **版本:** v 1.0.0
 */
data class SKUInfo(
    val price: Long,
    val imageUrl: String,
    val specList: List<SpecInfo>,
    val specsName: String
) : SKU {

    /**
     * 用来判断当前SKU是否由某些属性组成。
     * @param specs 需要判断的属性，这里的参数不一定是当前SKU的全部属性。
     * @return 如果是由spec组成则返回true，否则返回false。
     */
    override fun hasSpecs(specs: Collection<Spec>): Boolean {
        return specList.map { it.specId }.containsAll(specs.map { it.specId })
    }
}