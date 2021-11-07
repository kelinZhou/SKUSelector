package com.kelin.skuselector.sku


/**
 * **描述:** SKU选择器，SKU算法的具体实现。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2021/11/5 3:52 PM
 *
 * **版本:** v 1.0.0
 */
class SKUSelector(
    /**
     * SKU列表。
     */
    private val skuList: List<SKU>,
    /**
     * 属性列表。
     */
    private val specList: List<Spec>
) {

    init {
        onSelectedNothing()
    }

    /**
     * 用来存放已选中的所有属性。需要保证同一组只能记录一个属性。
     */
    private val mSelected by lazy { ArrayList<Spec>() }

    val selected: List<Spec>
        get() = mSelected.toList()

    /**
     * 添加一个已选中的属性。
     */
    fun addSelected(spec: Spec) {
        //尝试删除同一组中的已选中的属性。
        mSelected.remove { it.groupId == spec.groupId }?.isSelected = false
        //将当前属性设置为选中状态。
        spec.isSelected = true
        //将当前选中的添加到已选中的集合中。
        mSelected.add(spec)
        //处理每个属性的禁用与可用。
        onSelectedChanged()
    }

    /**
     * 移除一个已选中的属性。
     */
    fun removeSelected(spec: Spec) {
        spec.isSelected = false
        if (mSelected.remove { it.specId == spec.specId } != null) {
            onSelectedChanged()
        }
    }

    /**
     * 处理用户没有选择任何属性时的逻辑。
     */
    private fun onSelectedNothing() {
        specList.forEach { it.isEnable = true }
    }

    /**
     * 核心方法，处理属性选择改变后的选中、禁用逻辑。
     */
    private fun onSelectedChanged() {
        if (mSelected.isEmpty()) {
            //如果当前没有选中任何一个属性，则全部属性可以点击。
            onSelectedNothing()
        } else {
            if (mSelected.size == 1) {
                val selected = mSelected.first()
                specList.forEach { spec ->
                    spec.isEnable = spec.isSelected || selected.groupId == spec.groupId || skuList.any {
                        it.hasSpecs(
                            listOf(
                                selected,
                                spec
                            )
                        )
                    }
                }
            } else {
                specList.forEach { spec ->
                    spec.isEnable = spec.isSelected || skuList.any { sku ->
                        sku.hasSpecs(
                            mSelected.toMutableList().apply {
                                remove { it.groupId == spec.groupId }
                                add(spec)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 为MutableList扩展一个根据条件移除元素的方法。
 */
inline fun <E> MutableList<E>.remove(filter: (E) -> Boolean): E? {
    iterator().also {
        while (it.hasNext()) {
            val next = it.next()
            if (filter(next)) {
                it.remove()
                return next
            }
        }
    }
    return null
}